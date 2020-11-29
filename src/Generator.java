import java.util.*;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;

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

//	static void output(int id, ArrayList<Integer> ptsto)
//	{
//		String ans = Integer.toString(id);
//		ans += " :";
//		for(Integer i : ptsto) ans += (" " + Integer.toString(i));
//		ans += "\n";
//		System.out.print(ans);
//	}

	static void output(Map<Integer, List<Integer>> ptsto)
	{
		String ans = "";

		ArrayList<Integer> query = new ArrayList<>(ptsto.keySet());
		Collections.sort(query);
		for(Integer id : query)
		{
			String temp = Integer.toString(id);
			temp += ":";
			for(Integer i : ptsto.get(id)) temp += (" " + Integer.toString(i));
			ans += (temp + "\n");
		}
		try {
			PrintStream ps = new PrintStream(
					new FileOutputStream(new File("./result.txt")));
			ps.println(ans);
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
