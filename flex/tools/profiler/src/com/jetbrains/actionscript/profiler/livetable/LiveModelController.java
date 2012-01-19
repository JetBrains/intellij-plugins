package com.jetbrains.actionscript.profiler.livetable;

import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.actionscript.profiler.base.SortableListTreeTableModel;
import com.jetbrains.actionscript.profiler.sampler.*;
import com.jetbrains.actionscript.profiler.util.ResolveUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: Fedor.Korotkov
 */
public class LiveModelController implements ObjectSampleHandler {
  private final List<Sample> cache = new LinkedList<Sample>();
  private final ConcurrentLinkedQueue<Sample> queue = new ConcurrentLinkedQueue<Sample>();

  private final List<SizeInfoNode> filteredClasses = new ArrayList<SizeInfoNode>();
  private GlobalSearchScope scope;

  public void updateScope(GlobalSearchScope scope) {
    this.scope = scope;
  }

  public void apply(SortableListTreeTableModel model) {
    while (!queue.isEmpty()) {
      cache.add(queue.poll());
    }
    final Iterator<Sample> iterator = cache.iterator();
    while (iterator.hasNext()) {
      final Sample sample = iterator.next();
      if (sample instanceof CreateObjectSample) {
        applyCreate(model, (CreateObjectSample)sample);
        iterator.remove();
      }
      else if (sample instanceof DeleteObjectSample && applyDelete(model, (DeleteObjectSample)sample)) {
        iterator.remove();
      }
    }
    removeUselessNodes((MutableTreeNode)model.getRoot());

    filterByScope(model);
  }

  private void filterByScope(SortableListTreeTableModel model) {
    final MutableTreeNode root = (MutableTreeNode)model.getRoot();
    int i = 0;
    Iterator<SizeInfoNode> iterator = filteredClasses.iterator();
    while (iterator.hasNext()) {
      final SizeInfoNode node = iterator.next();
      if (scope == null || ResolveUtil.findClassByQName(node.getQName(), scope) != null) {
        root.insert(node, root.getChildCount());
        iterator.remove();
        ++i;
      }
    }
    while (i < root.getChildCount()) {
      final SizeInfoNode child = (SizeInfoNode)root.getChildAt(i);
      if (scope != null && ResolveUtil.findClassByQName(child.getQName(), scope) == null) {
        root.remove(i);
        filteredClasses.add(child);
      }
      else {
        ++i;
      }
    }
  }

  private static void removeUselessNodes(MutableTreeNode root) {
    for (int i = 0; i < root.getChildCount(); ++i) {
      MutableTreeNode child = (MutableTreeNode)root.getChildAt(i);
      if (child instanceof SizeInfoNode) {
        if (((SizeInfoNode)child).getCount() <= 0) {
          root.remove(child);
          continue;
        }
      }
      removeUselessNodes(child);
    }
  }

  private void applyCreate(SortableListTreeTableModel model,
                           CreateObjectSample sample) {
    final String className = sample.className;
    final DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
    SizeInfoNode classNode = findChildByValue(root, className);
    if (classNode == null) {
      classNode = new SizeInfoNode(sample.className, null, 0, 0);
      root.insert(classNode, root.getChildCount());
    }
    classNode.incSize(sample.size);

    SizeInfoNode node = classNode;
    for (FrameInfo frameInfo : sample.frames) {
      SizeInfoNode frameNode = findChildByValue(node, frameInfo.getQName());
      if (frameNode == null) {
        frameNode = new SizeInfoNode(frameInfo.toSimpleString(), frameInfo, 0, 0);
        node.insert(frameNode, node.getChildCount());
      }
      node = frameNode;
      node.incSize(sample.size);
    }
  }

  private boolean applyDelete(SortableListTreeTableModel model,
                              DeleteObjectSample sample) {
    final List<SizeInfoNode> nodesForUpdate = new ArrayList<SizeInfoNode>();
    final String className = sample.className;
    final DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
    SizeInfoNode classNode = findChildByValue(root, className);
    if (classNode == null) {
      return false;
    }
    nodesForUpdate.add(classNode);

    SizeInfoNode node = classNode;
    for (FrameInfo frameInfo : sample.frames) {
      final SizeInfoNode frameNode = findChildByValue(node, frameInfo.getQName());
      if (frameNode == null) {
        return false;
      }
      nodesForUpdate.add(frameNode);
      node = frameNode;
    }
    for (SizeInfoNode sizeInfoNode : nodesForUpdate) {
      sizeInfoNode.decSize(sample.size);
    }
    return true;
  }

  @Nullable
  private SizeInfoNode findChildByValue(TreeNode root, String name) {
    for (int i = 0; i < root.getChildCount(); ++i) {
      TreeNode child = root.getChildAt(i);
      if (child instanceof SizeInfoNode) {
        if (name.equals(((SizeInfoNode)child).getQName())) {
          return (SizeInfoNode)child;
        }
      }
    }
    for (SizeInfoNode filteredNode : filteredClasses) {
      if (name.equals(filteredNode.getQName())) {
        return filteredNode;
      }
    }
    return null;
  }

  @Override
  public void processCreateSample(CreateObjectSample createObjectSample) {
    if (createObjectSample.className != null) {
      queue.add(createObjectSample);
    }
  }

  @Override
  public void processDeleteSample(DeleteObjectSample deleteObjectSample) {
    if (deleteObjectSample.className != null) {
      queue.add(deleteObjectSample);
    }
  }
}
