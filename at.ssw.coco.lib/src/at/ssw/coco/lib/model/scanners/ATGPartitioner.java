package at.ssw.coco.lib.model.scanners;

import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTERS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_CHARACTERS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_COMMENTS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_IGNORECASE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_IGNORE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_IMPORTS;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_PARSER_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_PRAGMAS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_PRODUCTIONS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_TOKENS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.COMMENTS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.COMPILER_IDENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.DEFAULT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.IGNORECASE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.IGNORE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.IMPORTS;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_CHARACTERS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_COMMENTS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_COMPILER_IDENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_IGNORECASE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_IGNORE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_IMPORTS;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_PARSER_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_PRAGMAS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_PRODUCTIONS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_TOKENS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.PARSER_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.PRAGMAS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.PRODUCTIONS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_CHARACTERS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_COMMENTS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_COMPILER_IDENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_IGNORECASE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_IGNORE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_IMPORTS;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_PARSER_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_PRAGMAS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_PRODUCTIONS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_TOKENS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_CHARACTERS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_COMMENTS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_IGNORECASE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_IGNORE_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_IMPORTS;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_PARSER_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_PRAGMAS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_PRODUCTIONS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_TOKENS_SEGMENT;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.TOKENS_SEGMENT;

import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_PRAGMAS_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_PRAGMAS_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_PRAGMAS_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_PRAGMAS_INLINE_CODE;

import static at.ssw.coco.lib.model.scanners.ATGPartitions.SINGLE_LINE_COMMENT_PRODUCTIONS_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.MULTI_LINE_COMMENT_PRODUCTIONS_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.STRING_PRODUCTIONS_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.CHARACTER_PRODUCTIONS_INLINE_CODE;

import static at.ssw.coco.lib.model.scanners.ATGPartitions.PRAGMAS_INLINE_CODE;
import static at.ssw.coco.lib.model.scanners.ATGPartitions.PRODUCTIONS_INLINE_CODE;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to cover non-default open partitions.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */

public class ATGPartitioner {
	private static Map<String, String> openPartitionMap;
	
	/**
	 * @return the openPartitionMap
	 */
	public static Map<String, String> getOpenPartitionMap() {
		initializePartitionMap();
		return openPartitionMap;
	}

	private static void initializePartitionMap() {
		openPartitionMap = new HashMap<String, String>();
		openPartitionMap.put(SINGLE_LINE_COMMENT,DEFAULT);
		openPartitionMap.put(MULTI_LINE_COMMENT, DEFAULT);
		openPartitionMap.put(CHARACTER, DEFAULT);
		openPartitionMap.put(STRING, DEFAULT);
		
		openPartitionMap.put(SINGLE_LINE_COMMENT_IGNORECASE_SEGMENT, IGNORECASE_SEGMENT);
		openPartitionMap.put(MULTI_LINE_COMMENT_IGNORECASE_SEGMENT, IGNORECASE_SEGMENT);
		openPartitionMap.put(CHARACTER_IGNORECASE_SEGMENT, IGNORECASE_SEGMENT);
		openPartitionMap.put(STRING_IGNORECASE_SEGMENT, IGNORECASE_SEGMENT);
		
		openPartitionMap.put(SINGLE_LINE_COMMENT_CHARACTERS_SEGMENT, CHARACTERS_SEGMENT);
		openPartitionMap.put(MULTI_LINE_COMMENT_CHARACTERS_SEGMENT, CHARACTERS_SEGMENT);
		openPartitionMap.put(CHARACTER_CHARACTERS_SEGMENT, CHARACTERS_SEGMENT);
		openPartitionMap.put(STRING_CHARACTERS_SEGMENT, CHARACTERS_SEGMENT);
		
		openPartitionMap.put(SINGLE_LINE_COMMENT_TOKENS_SEGMENT, TOKENS_SEGMENT);
		openPartitionMap.put(MULTI_LINE_COMMENT_TOKENS_SEGMENT, TOKENS_SEGMENT);
		openPartitionMap.put(CHARACTER_TOKENS_SEGMENT, TOKENS_SEGMENT);
		openPartitionMap.put(STRING_TOKENS_SEGMENT, TOKENS_SEGMENT);
		
		openPartitionMap.put(SINGLE_LINE_COMMENT_PRAGMAS_SEGMENT, PRAGMAS_SEGMENT);
		openPartitionMap.put(MULTI_LINE_COMMENT_PRAGMAS_SEGMENT, PRAGMAS_SEGMENT);
		openPartitionMap.put(CHARACTER_PRAGMAS_SEGMENT, PRAGMAS_SEGMENT);
		openPartitionMap.put(STRING_PRAGMAS_SEGMENT, PRAGMAS_SEGMENT);
		
		openPartitionMap.put(SINGLE_LINE_COMMENT_COMMENTS_SEGMENT, COMMENTS_SEGMENT);
		openPartitionMap.put(MULTI_LINE_COMMENT_COMMENTS_SEGMENT, COMMENTS_SEGMENT);
		openPartitionMap.put(CHARACTER_COMMENTS_SEGMENT, COMMENTS_SEGMENT);
		openPartitionMap.put(STRING_COMMENTS_SEGMENT, COMMENTS_SEGMENT);
		
		openPartitionMap.put(SINGLE_LINE_COMMENT_IGNORE_SEGMENT, IGNORE_SEGMENT);
		openPartitionMap.put(MULTI_LINE_COMMENT_IGNORE_SEGMENT, IGNORE_SEGMENT);
		openPartitionMap.put(CHARACTER_IGNORE_SEGMENT, IGNORE_SEGMENT);
		openPartitionMap.put(STRING_IGNORE_SEGMENT, IGNORE_SEGMENT);
		
		openPartitionMap.put(SINGLE_LINE_COMMENT_PRODUCTIONS_SEGMENT, PRODUCTIONS_SEGMENT);
		openPartitionMap.put(MULTI_LINE_COMMENT_PRODUCTIONS_SEGMENT, PRODUCTIONS_SEGMENT);
		openPartitionMap.put(CHARACTER_PRODUCTIONS_SEGMENT, PRODUCTIONS_SEGMENT);
		openPartitionMap.put(STRING_PRODUCTIONS_SEGMENT, PRODUCTIONS_SEGMENT);

		openPartitionMap.put(SINGLE_LINE_COMMENT_IMPORTS, IMPORTS);
		openPartitionMap.put(MULTI_LINE_COMMENT_IMPORTS, IMPORTS);
		openPartitionMap.put(CHARACTER_IMPORTS, IMPORTS);
		openPartitionMap.put(STRING_IMPORTS, IMPORTS);

		openPartitionMap.put(SINGLE_LINE_COMMENT_PARSER_CODE, PARSER_CODE);
		openPartitionMap.put(MULTI_LINE_COMMENT_PARSER_CODE, PARSER_CODE);
		openPartitionMap.put(CHARACTER_PARSER_CODE, PARSER_CODE);
		openPartitionMap.put(STRING_PARSER_CODE, PARSER_CODE);

		openPartitionMap.put(SINGLE_LINE_COMMENT_INLINE_CODE, INLINE_CODE);
		openPartitionMap.put(MULTI_LINE_COMMENT_INLINE_CODE, INLINE_CODE);
		openPartitionMap.put(CHARACTER_INLINE_CODE, INLINE_CODE);
		openPartitionMap.put(STRING_INLINE_CODE, INLINE_CODE);
		
		openPartitionMap.put(SINGLE_LINE_COMMENT_PRAGMAS_INLINE_CODE, PRAGMAS_INLINE_CODE);
		openPartitionMap.put(MULTI_LINE_COMMENT_PRAGMAS_INLINE_CODE, PRAGMAS_INLINE_CODE);
		openPartitionMap.put(CHARACTER_PRAGMAS_INLINE_CODE, PRAGMAS_INLINE_CODE);
		openPartitionMap.put(STRING_PRAGMAS_INLINE_CODE, PRAGMAS_INLINE_CODE);
		
		openPartitionMap.put(SINGLE_LINE_COMMENT_PRODUCTIONS_INLINE_CODE, PRODUCTIONS_INLINE_CODE);
		openPartitionMap.put(MULTI_LINE_COMMENT_PRODUCTIONS_INLINE_CODE, PRODUCTIONS_INLINE_CODE);
		openPartitionMap.put(CHARACTER_PRODUCTIONS_INLINE_CODE, PRODUCTIONS_INLINE_CODE);
		openPartitionMap.put(STRING_PRODUCTIONS_INLINE_CODE, PRODUCTIONS_INLINE_CODE);

		openPartitionMap.put(SINGLE_LINE_COMMENT_COMPILER_IDENT, COMPILER_IDENT);
		openPartitionMap.put(MULTI_LINE_COMMENT_COMPILER_IDENT, COMPILER_IDENT);

//		openPartitionMap.put(INLINE_CODE_START, INLINE_CODE);
//		openPartitionMap.put(INLINE_CODE_END, DEFAULT);
	}
}
