package org.deri.rdf.browser.test.util;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.rdf.browser.model.AnnotatedResultItem;

public class AssertionUtil {

	public static void assertEqualItemLists(List<AnnotatedResultItem> items, List<AnnotatedResultItem> expected) {
		List<AnnotatedResultItem> workingCopy = new ArrayList<AnnotatedResultItem>(expected);
		Set<String> faultyExtras = new HashSet<String>();
		Set<AnnotatedResultItem> nonequals = new HashSet<AnnotatedResultItem>();
		for(AnnotatedResultItem item:items){
			int index = workingCopy.indexOf(item);
			if(index==-1){
				faultyExtras.add(item.getValue().getValue().toString());
			}else{
				AnnotatedResultItem expectedItem = workingCopy.remove(index);
				if(expectedItem.equals(item)
						&& expectedItem.getCount() == item.getCount() 
						&& expectedItem.getEndpoints().length == item.getEndpoints().length){
					for(int i=0;i<expectedItem.getEndpoints().length;i++){
						if(!expectedItem.getEndpoints()[i].equals(item.getEndpoints()[i])){
							nonequals.add(item);
							break;
						}
					}
				}else{
					nonequals.add(item);
				}
			}
		}
		assertTrue(nonequals.isEmpty(), "these items were not as expected: " + nonequals);
		assertTrue(faultyExtras.isEmpty(),"these items were not expected in the result: " + faultyExtras);
		assertTrue(workingCopy.isEmpty(),"these elements were missing from the result: " + workingCopy);
	}
}
