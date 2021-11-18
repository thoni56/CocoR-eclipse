package at.ssw.coco.plEditor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class EditorPlugin extends AbstractUIPlugin {

	/**
	 * The constructor
	 */
	
	private static EditorPlugin plugin;
	
	 

	// The plug-in ID
	public static final String PLUGIN_ID = "at.ssw.cocoE.editor";
	
	public EditorPlugin() {
	}
	
	
	public static EditorPlugin getDefault(){
		return plugin;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin=null;
		super.stop(context);
	}
}
