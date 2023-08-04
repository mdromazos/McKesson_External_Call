package com.informatica.mdm.bes.domain;

public class BusinessRuleReturn {

		private String node;
		private String field;
		private String companyCode;
		private String purchaseOrg;
		private boolean mandatory;
		private boolean displayOnly;
		
		public BusinessRuleReturn(
					String node,
					String field,
					String companyCode,
					String purchaseOrg,
					boolean mandatory,
					boolean displayOnly
				) {
			this.node = node;
			this.field = field;
			this.companyCode = companyCode;
			this.purchaseOrg = purchaseOrg;
			this.mandatory = mandatory;
			this.displayOnly = displayOnly;
		}
		
		public String getNode() { return node; }
		public String getField() { return field; }
		public String getCompanyCode() { return companyCode; }
		public String getPurchaseOrg() { return purchaseOrg; }
		public boolean getMandatory() { return mandatory; }
		public boolean getDisplayOnly() { return displayOnly; }
	
}
