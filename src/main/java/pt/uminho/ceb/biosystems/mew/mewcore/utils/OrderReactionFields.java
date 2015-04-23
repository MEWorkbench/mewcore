package pt.uminho.ceb.biosystems.mew.mewcore.utils;

import java.util.ArrayList;
import java.util.List;

public enum OrderReactionFields {
	ID				(0, "DataBase", "DATABASE", Constants.DATABASE_VISIBLE), 
	NAME			(1, "Name", "NAME", Constants.NAME_VISIBLE), 
	EQUATION		(2,"Equation", "EQUATION", Constants.EQUATION_VISIBLE), 
	MAIN_EQ			(3, "Main Equation","MAIN EQUATION", Constants.MAIN_EQUATION_VISIBLE), 
	ENZYME			(4, "Enzyme", "ENZYME", Constants.ENZYME_VISIBLE), 
	PATHWAYS		(5,"Pathways", "PATHWAYS", Constants.PATHWAYS_VISIBLE), 
	KEGG_MAPS		(6, "Kegg Maps", "KEGG MAPS", Constants.KEGG_MAPS_VISIBLE), 
	REVERSIBILITY	(7, "Reversibility", "REVERSIBILITY", Constants.REVERSIBILITY_VISIBLE), 
	DELTAG			(8, "Delta G", "DELTAG", Constants.DELTAG_VISIBLE), 
	DELTAGERR		(9, "Delta G Error", "DELTAGERR", Constants.DELTAGERR_VISIBLE), 
	KEGGID			(10, "Kegg Id", "KEGGID", Constants.KEGGID_VISIBLE), 
	ARGONNEID		(11, "Argoneid", "ARGONNEID", Constants.ARGONNEID_VISIBLE), 
	MODELID			(12, "Model Id", "MODELID", Constants.MODELID_VISIBLE), 
	MODELS			(13, "Models", "MODELS", Constants.MODELS_VISIBLE);

	private final int index; // index
	private final String name; // name
	private final String id; // id
	private boolean visible;

	private OrderReactionFields(int index, String name, String id, boolean visible) {
		this.index = index;
		this.name = name;
		this.id = id;
		this.visible = visible;
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

	public boolean isVisible() {
		return visible;
	}
	
	public void setVisble(boolean visible) {
		this.visible = visible;
	}
	

	public static OrderReactionFields find(String id) {
		for (OrderReactionFields f : OrderReactionFields.values()) {
			if (f.getId().equals(id))
				return f;
		}
		return null;
	}

	public static OrderReactionFields findByName(String name) {
		for (OrderReactionFields f : OrderReactionFields.values()) {
			if (f.getName().equals(name))
				return f;
		}
		return null;
	}

	public static OrderReactionFields findByIndex(int ind) {
		switch (ind) {
		case 0:
			return ID;
		case 1:
			return NAME;
		case 2:
			return EQUATION;
		case 3:
			return MAIN_EQ;
		case 4:
			return ENZYME;
		case 5:
			return PATHWAYS;
		case 6:
			return KEGG_MAPS;
		case 7:
			return REVERSIBILITY;
		case 8:
			return DELTAG;
		case 9:
			return DELTAGERR;
		case 10:
			return KEGGID;
		case 11:
			return ARGONNEID;
		case 12:
			return MODELID;
		default:
			return MODELS;
		}
	}
	
	public static OrderReactionFields[] getVisibleFields() {
		List<OrderReactionFields> visibleFields = new ArrayList<OrderReactionFields>();
		
		for (OrderReactionFields orf : OrderReactionFields.values()) {
			if (orf.isVisible()) {
				visibleFields.add(orf);
			}
		}
		
		return visibleFields.toArray(new OrderReactionFields[] {}); 		
	}

	public static String[] getNames() {
		ArrayList<String> array = new ArrayList<String>();
		for (OrderReactionFields f : OrderReactionFields.values()) {
			array.add(f.getName());
		}
		return array.toArray(new String[] {});
	}
}
