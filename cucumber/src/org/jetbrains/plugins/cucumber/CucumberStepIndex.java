// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.util.io.DataInputOutputUtilRt;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.PsiDependentFileContent;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.text.StringSearcher;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public abstract class CucumberStepIndex extends FileBasedIndexExtension<Boolean, List<Integer>> {
  private static final List<String> STEP_KEYWORDS = Arrays.asList("Әмма", "Нәтиҗәдә", "Вә", "Әйтик", "Һәм", "Ләкин", "Әгәр",  "Und",
                                                                  "Angenommen", "Gegeben seien",  "Dann", "Aber", "Wenn", "Gegeben sei",
                                                                  "यदि", "तदा", "अगर", "और", "कदा", "परन्तु", "चूंकि", "जब", "किन्तु", "तथा", "पर", 
                                                                  "तब", "Dados", "Entao", "Dada", "Então", "Mas", "Dadas", "Dado",  
                                                                  "Quando", "E", "Bet", "Ir", "Tada",  "Kai", "Duota", "awer", "a", "an", 
                                                                  "wann", "mä", "ugeholl", "dann", "I", "Kada", "Kad", "Zadan", "Ali", 
                                                                  "Onda", "Zadano",  "Zadani", "Bet", "Kad", "Tad", "Ja", "Un",  "E", 
                                                                  "Sipoze ke", "Sipoze", "Epi",  "Men", "Le sa a", "Le", "Ak", "Lè", 
                                                                  "Sipoze Ke", "Lè sa a", "Ha", "Adott", "De", "Amikor", "És", "Majd", 
                                                                  "Akkor", "Amennyiben",  "并且", "而且", "假如", "同时", "当", "假设", "那么",
                                                                  "假定",  "但是", "Нехай", "Якщо", "І", "Припустимо, що", "Дано", 
                                                                  "Припустимо", "Коли", "Та", "Але", "То", "Тоді",  "А також",
                                                                  "It's just unbelievable", "Yeah nah",  "Too right",
                                                                  "But at the end of the day I reckon", "Y'know", "Maka",  "Tapi",
                                                                  "Ketika", "Dengan", "Dan", "اگر", "تب", "اور", "جب", "بالفرض", 
                                                                  "فرض کیا", "پھر", "لیکن", "Maar",  "En", "Dan", "Wanneer", "Gegewe",
                                                                  "Бирок", "Аммо", "Унда",  "Ва", "Лекин", "Агар", "Δεδομένου", "Τότε",
                                                                   "Και", "Αλλά", "Όταν", "Aye", "Let go and haul", "Gangway!", 
                                                                  "Avast!", "Blimey!", "When", "Then", "Given", "But",  "And", "Kaj",
                                                                   "Do", "Se", "Sed", "Donitaĵo", "Ef", "Þegar", "Þá",  "En", "Og",
                                                                  "Quando", "E", "Allora", "Dato", "Dati", "Date", "Data", "Ma", 
                                                                  "Cuando", "Dada", "Pero", "Entonces", "Dados", "Y", "Dadas", "Dado", 
                                                                  "Kui",  "Ja", "Eeldades", "Kuid", "Siis", "اذاً", "لكن", "و", 
                                                                  "متى", "بفرض", "ثم", "عندما", "Thì", "Khi", "Biết", "Và", "Cho", "Nhưng",
                                                                   "もし", "かつ", "但し", "ただし", "しかし", "ならば",  "前提", "A",
                                                                  "Anrhegedig a", "Pryd",  "Yna", "Ond", "هنگامی",  "با فرض", "آنگاه", 
                                                                  "اما", "و", "Dat fiind", "Dati fiind", "Atunci", "Dați fiind", "Dar", 
                                                                  "Si", "Când", "Daţi fiind", "Și",  "Cand", "Şi", "Date fiind", "Als",  
                                                                  "Maar", "Gegeven", "En", "Wanneer", "Stel", "Dan", "Gitt", "Så", "Når", 
                                                                  "Men", "Og", "Mutta", "Ja",  "Oletetaan", "Kun", "Niin", "Пусть", 
                                                                  "Допустим", "К тому же", "То", "Дано", "Когда", "Но", "Тогда", "Если", 
                                                                  "И",  "А", "Также", "Дадено", "И",  "То", "Когато", "Но", "Maka", 
                                                                  "Apabila", "Tapi", "Kemudian", "Dan", "Tetapi", "Diberi", "Bagi", 
                                                                  "Etant donnés", "Alors", "Étant données", "Etant donné", "Étant donnée", 
                                                                  "Lorsqu'", "Etant donnée", "Et", "Étant donné", "Quand", "Lorsque", 
                                                                  "Mais", "Soit", "Etant données",  "Étant donnés", "Njuk", "Tapi", 
                                                                  "Menawa", "Nalika", "Ananging", "Lan",  "Nanging", "Manawa", "Nalikaning", 
                                                                  "Banjur", "Givun", "Youse know when youse got", "Youse know like when", 
                                                                  "An", "Den youse gotta", "Buh", "Dun",  "Wun", "WEN", "I CAN HAZ", "BUT", 
                                                                  "AN", "DEN", "Potom", "Za predpokladu", "Tak", "Pokiaľ", "A zároveň", "A", 
                                                                  "Ak", "A taktiež", "Ale", "Keď", "A tiež", "Privzeto", "Ampak", "Takrat", 
                                                                  "Ko", "Nato", "Zaradi", "Ce", "Potem", "Če", "Ter", "Kadar", "Toda", 
                                                                  "Dano", "Podano", "Vendar", "In", "I", "Atesa", "Donada", "Aleshores", 
                                                                  "Cal", "Però", "Donat", "Quan",  "Atès", "ನೀಡಿದ", "ಮತ್ತು",  "ಆದರೆ", "ನಂತರ", 
                                                                  "ಸ್ಥಿತಿಯನ್ನು", "Så", "När", "Och", "Men", "Givet", "그러면",  "만약", "먼저", 
                                                                  "조건", "단", "만일", "하지만", "그리고", "Mais",  "E", "Dada", "Pero", "Dados", 
                                                                  "Logo", "Cando", "Dadas", "Dado", "Entón", "那麼", "假如", "而且", "同時", 
                                                                  "假設", "當", "假定",  "但是", "並且", "And y'all", "But y'all", "When y'all",  
                                                                  "Then y'all", "Given y'all", "Zadate", "I", "Kad", "Zatati", "Ali", 
                                                                  "Kada", "Onda",  "Zadato", "Pak", "A", "Ale", "A také", "Když", 
                                                                  "Za předpokladu", "Pokud", "ਅਤੇ", "ਜਿਵੇਂ ਕਿ", "ਪਰ", "ਜੇਕਰ", "ਜਦੋਂ", "ਤਦ", 
                                                                  "Задато", "Кад", "Задати", "Када", "Задате", "Али", "Онда", "И",  
                                                                  "ghu' noblu'", "DaH ghu' bejlu'", "latlh", "qaSDI'", "'ach", "'ej", "'a",  
                                                                  "vaj", "กำหนดให้", "แต่", "และ",  "เมื่อ", "ดังนั้น", "మరియు", "అప్పుడు", 
                                                                  "ఈ పరిస్థితిలో", "చెప్పబడినది", "కాని", "I",  "Gdy", "Kiedy", "Wtedy", "Ale", 
                                                                  "Jeżeli", "Jeśli", "Mając", "Zakładając", "Oraz", "Og", "Så", "Når",
                                                                   "Men", "Givet", "כאשר", "וגם", "אז",  "בהינתן", "אבל", "אזי",
                                                                  "Ond", "Ðurh", "Ða", "Ða ðe", "Ac", "Thurh", "Þa", "7", "Þa þe", "Tha",
                                                                  "Þurh",  "Tha the", "Ama", "Fakat", "O zaman",  "Ve", "Eğer ki",
                                                                  "Diyelim ki");
  
  @NotNull
  @Override
  public DataIndexer<Boolean, List<Integer>, FileContent> getIndexer() {
    return inputData -> {
      CharSequence text = inputData.getContentAsText();
      if (!hasCucumberImport(text)) {
        return Collections.emptyMap();
      }

      LighterAST lighterAst = ((PsiDependentFileContent)inputData).getLighterAST();
      List<Integer> result = getStepDefinitionOffsets(lighterAst, text);
      Map<Boolean, List<Integer>> resultMap = new HashMap<>();
      resultMap.put(true, result);
      return resultMap;
    };
  }

  @NotNull
  @Override
  public KeyDescriptor<Boolean> getKeyDescriptor() {
    return BooleanDataDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public DataExternalizer<List<Integer>> getValueExternalizer() {
    return DATA_EXTERNALIZER;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  protected abstract String[] getPackagesToScan();
  
  private boolean hasCucumberImport(@NotNull CharSequence text) {
    for (String pkg : getPackagesToScan()) {
      StringSearcher searcher = new StringSearcher(pkg, true, true);
      if (searcher.scan(text) > 0) {
        return true;
      }
    }
    return false;
  }

  protected static boolean isStepDefinitionCall(@NotNull LighterASTNode methodName, @NotNull CharSequence text) {
    return STEP_KEYWORDS.contains(text.subSequence(methodName.getStartOffset(), methodName.getEndOffset()).toString());
  }

  protected static boolean isStringLiteral(@NotNull LighterASTNode element, @NotNull CharSequence text) {
    return text.charAt(element.getStartOffset()) == '"';
  }

  protected static boolean isNumber(@NotNull LighterASTNode element, @NotNull CharSequence text) {
    for (int i = element.getStartOffset(); i < element.getEndOffset(); i++) {
      if (!Character.isDigit(text.charAt(i))) {
        return false;
      }
    }
    
    return element.getTextLength() > 0;
  }

  protected abstract List<Integer> getStepDefinitionOffsets(@NotNull LighterAST lighterAst, @NotNull CharSequence text);

  private static final DataExternalizer<List<Integer>> DATA_EXTERNALIZER = new DataExternalizer<>() {
    @Override
    public void save(@NotNull DataOutput out, List<Integer> value) throws IOException {
      DataInputOutputUtilRt.writeSeq(out, value, descriptor -> {
        DataInputOutputUtilRt.writeINT(out, descriptor.intValue());
      });
    }

    @Override
    public List<Integer> read(@NotNull DataInput in) throws IOException {
      return DataInputOutputUtilRt.readSeq(in, () -> DataInputOutputUtilRt.readINT(in));
    }
  };
}
