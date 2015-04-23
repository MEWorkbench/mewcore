package pt.uminho.ceb.biosystems.mew.mewcore.utils;

public enum OrderCompoundFields {

	ID			(0, "DataBase", "DATABASE"), 
	NAME		(1, "Name", "NAME"), 
	FORMULA		(2,"Formula", "FORMULA"), 
	CHARGE		(3, "Charge", "CHARGE"), 
	STRINGCODE	(4,"string code", "STRINGCODE"), 
	SEARCHNAME	(5, "Search name","SEARCHNAME"), 
	KEGGID		(6, "Kegg Id", "KEGGID"), 
	ARGONNEID	(7,"Argoneid", "ARGONNEID"), 
	MODELID		(8, "Model Id", "MODELID"),
	COMPARTMENT	(9, "Model Id", "COMPARTMENT");

	private final int index; // index
	private final String name; // name
	private final String id; // id 

	private OrderCompoundFields(int index, String name, String id) {
		this.index = index;
		this.name = name;
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public static OrderCompoundFields find(String id) {
		for (OrderCompoundFields f : OrderCompoundFields.values()) {
			if (f.getId().equals(id))
				return f;
		}
		return null;
	}
}
