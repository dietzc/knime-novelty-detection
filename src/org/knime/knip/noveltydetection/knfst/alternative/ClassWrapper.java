package org.knime.knip.noveltydetection.knfst.alternative;

import java.util.ArrayList;

public class ClassWrapper {
	private String name;
	private int count;
	
	public ClassWrapper(String name, int count) {
		this.name = name;
		this.count = count;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public static ArrayList<ClassWrapper> classes(String[] labels) {
		ArrayList<ClassWrapper> ret = new ArrayList<ClassWrapper>();
		int count = 0;
		String nameOld = labels[0];
		for (int i = 0; i < labels.length; i++) {
			String nameNew = labels[i];
			if (!nameOld.equals(nameNew)) {
				ret.add(new ClassWrapper(nameOld, count));
				nameOld = nameNew;
				count = 1;
			} else {
				count++;
			}
		}
		
		ret.add(new ClassWrapper(nameOld, count));
		
		return ret;
	}
}
