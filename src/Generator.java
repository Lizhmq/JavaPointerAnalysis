import java.util.Map;
import java.util.Set;



import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import soot.Local;

public class Generator {

	protected String answer;

	// Object[] stores pair<int, local>
	Generator(ArrayList<Object []> querys, Map<Local, Set<Integer>> ptsto) {
		Iterator<Object []> it = querys.iterator();
		while (it.hasNext()) {
			Object[] l = it.next();
			answer += ((Integer)l[0]).toString() + ":";
			Set<Integer> s = ptsto.get((Local)l[1]);
			for (Integer i: s)
				answer += " " + i.toString();
			answer += '\n';	
		}
	}

	public void WriteTo(String path) {
		try {
			PrintStream ps = new PrintStream(
				new FileOutputStream(new File(path)));
			ps.println(answer);
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}	
}
