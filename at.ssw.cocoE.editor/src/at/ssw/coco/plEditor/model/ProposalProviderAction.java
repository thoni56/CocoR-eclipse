package at.ssw.coco.plEditor.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

import at.ssw.coco.plEditor.EditorPlugin;
import at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants;

public class ProposalProviderAction extends Action implements IActionDelegate {

	/** The selected item */
	private ISelection fSelection;
	
	@Override
	public void run(IAction action) {
		if (!(fSelection instanceof IStructuredSelection)) {
			return;
		}
		//the currently selected atg file
		IJavaElement proposalProviderFile = (IJavaElement) (((IStructuredSelection) fSelection).getFirstElement());
		
		IResource res = proposalProviderFile.getResource();
		
		File sourcerFile = res.getLocation().toFile();
		
		String outputFolderPath = EditorPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.fieldName);
		
		String editorFolder = res.getName();
		editorFolder = editorFolder.substring(0, editorFolder.lastIndexOf('_'));
		editorFolder = editorFolder.toLowerCase();
		
		
		File targetFile = new File(outputFolderPath+Path.SEPARATOR+editorFolder+Path.SEPARATOR+res.getName());
		
		try{
			FileReader in = new FileReader(sourcerFile);
			BufferedReader br = new BufferedReader(in);
			FileWriter out = new FileWriter(targetFile);
			String line;
			
			while((line = br.readLine())!=null){
				String[] words = line.split("\\s+");
				if(words.length>0 && words[0].equals("package")){
					break;
				}
				out.write(line);
			}
			
			while((line = br.readLine())!=null){
				out.write(line);
			}
			
		    in.close();
		    out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(null, null, null, targetFile.getAbsolutePath());
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;		
		
	}

}
