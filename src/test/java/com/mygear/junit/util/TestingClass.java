package com.mygear.junit.util;

import java.util.Date;

import com.mygear.tools.Unmarshaller;

public class TestingClass {

	public static void main(String[] args) {
		new Unmarshaller().setDepthTreshold(5).setExportFilePath("d:\\Projects\\MyGear\\Eclipse_workspace\\MyGear\\src\\file.xml").m(new Date());

	}

}
