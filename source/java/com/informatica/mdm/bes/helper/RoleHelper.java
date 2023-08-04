package com.informatica.mdm.bes.helper;

import java.util.List;

public class RoleHelper {

	/**
	 * Check the users role to determine if the user is a Supplier or an internal McKesson user.
	 * No Need to check SupplierView because they will never be editting the record.
	 * @return if the user is a Supplier
	 */
	public static boolean isSupplier(List<String> roles) {
		if (roles == null || roles.isEmpty())
			return false;
		
		String rolesStr = String.join(",", roles);
		
		if (rolesStr.contains("SupplierAdministrator") || rolesStr.contains("SupplierUser") || rolesStr.contains("Supplier Administrators")) {
			return true;
		} else {
			return false;
		}
	}
}
