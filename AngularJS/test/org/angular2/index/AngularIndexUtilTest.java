// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.junit.Assert;

public class AngularIndexUtilTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "injections";
  }

  public void testHasAngularJS2() {
    myFixture.configureByText("common.metadata.json",
                              "{\"__symbolic\": \"module\",\"version\": 3,\"metadata\": {\"NgForOf\":{\"__symbolic\":\"class\",\"arity\":1,\"decorators\":[{\"__symbolic\":\"call\",\"expression\":{\"__symbolic\":\"reference\",\"module\":\"@angular/core\",\"name\":\"Directive\"},\"arguments\":[{\"selector\":\"[ngFor][ngForOf]\"}]}],\"members\":{\"ngForOf\":[{\"__symbolic\":\"property\",\"decorators\":[{\"__symbolic\":\"call\",\"expression\":{\"__symbolic\":\"reference\",\"module\":\"@angular/core\",\"name\":\"Input\"}}]}],\"ngForTrackBy\":[{\"__symbolic\":\"property\",\"decorators\":[{\"__symbolic\":\"call\",\"expression\":{\"__symbolic\":\"reference\",\"module\":\"@angular/core\",\"name\":\"Input\"}}]}],\"__ctor__\":[{\"__symbolic\":\"constructor\",\"parameters\":[{\"__symbolic\":\"reference\",\"module\":\"@angular/core\",\"name\":\"ViewContainerRef\"},{\"__symbolic\":\"reference\",\"name\":\"TemplateRef\",\"module\":\"@angular/core\",\"arguments\":[{\"__symbolic\":\"reference\",\"name\":\"NgForOfContext\"}]},{\"__symbolic\":\"reference\",\"module\":\"@angular/core\",\"name\":\"IterableDiffers\"}]}],\"ngForTemplate\":[{\"__symbolic\":\"property\",\"decorators\":[{\"__symbolic\":\"call\",\"expression\":{\"__symbolic\":\"reference\",\"module\":\"@angular/core\",\"name\":\"Input\"}}]}],\"ngOnChanges\":[{\"__symbolic\":\"method\"}],\"ngDoCheck\":[{\"__symbolic\":\"method\"}],\"_applyChanges\":[{\"__symbolic\":\"method\"}],\"_perViewChange\":[{\"__symbolic\":\"method\"}]}}}}");
    Assert.assertNotNull(AngularIndexUtil.resolve(myFixture.getProject(), AngularDirectivesIndex.KEY, "ngFor"));
  }
}
