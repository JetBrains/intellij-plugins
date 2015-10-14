(function() {

    var ns = Ext.ns('ccm.widgets');


    var showGroupCustomerSelection = function() {
        var window = new ccm.widgets.GroupCustomerSelectionWindow({selectedCompanyId : '10000'});
        window.s<caret>how();
    };

    // register xtype
    Ext.reg('ccm.widgets.RiskInfoPanel', ns.RiskInfoPanel);
})();

function show() {}
