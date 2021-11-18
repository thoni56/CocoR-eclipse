package at.ssw.coco.plEditor.features.syntaxHighlighting;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * Extends (modifies) a <code> DefaultDamagerRepairer <code> to create TextPresentation according to CLNGEditor's needs
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class NewDamagerRepairer extends DefaultDamagerRepairer {

	private long pref = 0;
	
	/**
	 * The constructor
	 * @param scanner The token Scanner used for SyntaxHighlighting
	 */
	public NewDamagerRepairer(ITokenScanner scanner) {
		super(scanner);
	}
	
	
	@Override
	public void createPresentation(TextPresentation presentation, ITypedRegion region) {
			
		long time = System.currentTimeMillis();
		if(time-pref < 100){
	
//			pref = time;
			
			return;
		}
		
		pref = time;
	
		createPres(presentation, region);
		
		
	}
	
	private void createPres(TextPresentation presentation, ITypedRegion region){
		// This method is almost equivalent to the implementation of the superclass, but with a single difference. (see below) 
		
				if (fScanner == null) {
					// will be removed if deprecated constructor will be removed
					addRange(presentation, region.getOffset(), region.getLength(), fDefaultTextAttribute);
					return;
				}

				int lastStart= region.getOffset();
				int length= 0;
				boolean firstToken= true;
				IToken lastToken= Token.UNDEFINED;
				TextAttribute lastAttribute= getTokenTextAttribute(lastToken);

				fScanner.setRange(fDocument, lastStart, region.getLength());

				while (true) {
					IToken token= fScanner.nextToken();
					if (token.isEOF())
						break;


					// Since the Coco Scanner already delivers complete tokens and skips comments and white space characters,
					// every single token has to be processed, even if the attribute is identical
					// (implementation of the superclass simply increases the length of the last token in that specific case)
					TextAttribute attribute= getTokenTextAttribute(token);
						
					if (!firstToken)
						addRange(presentation, lastStart, length, lastAttribute);
					firstToken= false;
					lastToken= token;
					lastAttribute= attribute;
					lastStart= fScanner.getTokenOffset();
					length= fScanner.getTokenLength();
					
				}

				addRange(presentation, lastStart, length, lastAttribute);
	}
	
	abstract class SyntaxTask extends TimerTask{
		protected TextPresentation presentation;
		protected ITypedRegion region;
		
		public SyntaxTask(TextPresentation presentation, ITypedRegion region){
			this.presentation = presentation;
			this.region = region;
		}
		
		
	}
}
