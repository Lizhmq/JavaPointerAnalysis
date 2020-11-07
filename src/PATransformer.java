import java.util.*;

import soot.*;
import soot.jimple.*;

public class PATransformer extends SceneTransformer {
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		System.out.println(phaseName);
		System.out.println(Scene.v().getApplicationClasses());
	}
}
