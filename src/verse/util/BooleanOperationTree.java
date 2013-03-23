/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: Devin Fisher
 * Created: Apr 12, 2011
 */
package verse.util;

public class BooleanOperationTree 
{
	public enum BooleanOperation
	{
		AND,
		OR;
		
		public boolean evaluateOperation(boolean left, boolean right)
		{
			switch(this)
			{
			case AND:
				return left && right;
			case OR:
			default:
				return left || right;
			}
		}
	}
	
	public static class BinaryNode
	{
		public final BinaryNode LEFT;
		public final BinaryNode RIGHT;
		public final BooleanOperation OPERATION;
		
		public final BooleanExpression EXPRESSION;
		
		public BinaryNode(BinaryNode left, BinaryNode right, BooleanOperation operation)
		{
			LEFT = left;
			RIGHT = right;
			OPERATION = operation;
			EXPRESSION = null;
		}
		
		public BinaryNode(BooleanExpression expression) 
		{
			LEFT = null;
			RIGHT = null;
			OPERATION = null;
			EXPRESSION = expression;
		}
	}
	
	public interface BooleanExpression
	{
		public boolean evaluate(Object input);
	}
	
	public static boolean evaluateTree(BinaryNode node) throws InvalidTreeException
	{
		return evaluateTree(node, null);
	}
	
	public static boolean evaluateTree(BinaryNode node, Object input) throws InvalidTreeException
	{
		if(node.LEFT == null && node.RIGHT == null)
		{
			if(node.EXPRESSION != null)
			{
				return node.EXPRESSION.evaluate(input);
			}
			else
			{
				throw new InvalidTreeException("Leaf nodes must define an Expression");
			}
		}
		
		if(node.LEFT == null || node.RIGHT == null)
		{
			throw new InvalidTreeException("Only leaf nodes can have null children");
		}
		
		if(node.OPERATION == null)
		{
			throw new InvalidTreeException("Only leaf nodes can have a null operation");
		}
		
		return node.OPERATION.evaluateOperation(evaluateTree(node.LEFT, input), evaluateTree(node.RIGHT, input));
	}

	public static class InvalidTreeException extends Exception
	{
		public InvalidTreeException(String string) {
			super(string);
		}

		private static final long serialVersionUID = -5574005209271161997L;
	}
}
