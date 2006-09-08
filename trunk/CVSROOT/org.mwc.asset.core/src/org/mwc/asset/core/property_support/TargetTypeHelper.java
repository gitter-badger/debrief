/**
 * 
 */
package org.mwc.asset.core.property_support;

import java.text.DecimalFormat;
import java.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.mwc.cmap.core.property_support.EditorHelper;

import ASSET.Models.Decision.TargetType;
import ASSET.Participants.Category;

public class TargetTypeHelper extends EditorHelper
{

	/**
	 * remember how to format items on line
	 */
	protected static DecimalFormat _floatFormat = new DecimalFormat("0.0000");

	/**
	 * constructor. just declare our object type
	 */
	public TargetTypeHelper()
	{
		super(TargetType.class);
	}

	/**
	 * we define a custom cell editor just to get the "Paste" button
	 * 
	 * @param parent
	 * @return
	 */
	public CellEditor getCellEditorFor(Composite cellParent)
	{
		DialogCellEditor res = new TargetTypeCellEditor(cellParent);

		return res;
	}


	public ILabelProvider getLabelFor(Object currentValue)
	{
		ILabelProvider label1 = new LabelProvider()
		{
			public String getText(Object element)
			{
				TargetType val = (TargetType) element;
				return val.toString();
			}

			public Image getImage(Object element)
			{
				return null;
			}

		};
		return label1;
	}

	/**
	 * custom cell editor which re-purposes button used to open dialog as a paste
	 * button
	 * 
	 * @author ian.mayo
	 */
	protected static class TargetTypeDialog extends org.eclipse.jface.dialogs.Dialog
	{

		/**
		 * and the drop-down units bit
		 */
		List _myForce;

		/**
		 * and the drop-down units bit
		 */
		List _myEnvironment;

		/**
		 * and the drop-down units bit
		 */
		List _myType;		
		
		final TargetType _current;
		
		/**
		 * our tooltips
		 */
		final private String _forceTip = "the force of the subject participant(s)";
		final private String _environmentTip = "the environment of the subject participant(s)";
		final private String _typeTip = "the type of the subject participant(s)";		
		
		public TargetTypeDialog(TargetType current, Shell parent)
		{
			super(parent);
			_current = current;
		}

		/**
		 * @param parent
		 * @return
		 */
		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite) super.createDialogArea(parent);
			_myForce = new List(composite, SWT.NONE);
			_myForce.setToolTipText(_forceTip);
			_myForce.setItems(getForces());

			_myEnvironment = new List(composite, SWT.DROP_DOWN);
			_myEnvironment.setToolTipText(_environmentTip);
			_myEnvironment.setItems(getEnvironments());

			_myType = new List(composite, SWT.DROP_DOWN);
			_myType.setToolTipText(_typeTip);
			_myType.setItems(getTypes());
			return  composite;
		}
		

		protected void doSetValue(Object value)
		{
			TargetType _myCat = (TargetType) value;

			Vector forces = new Vector(0,1);
			Vector types = new Vector(0,1);
			Vector envs = new Vector(0,1);
			
			// ok, sort out the forces
			Collection targetTypes = _myCat.getTargets();
			for (Iterator iter = targetTypes.iterator(); iter.hasNext();)
			{
				String type = (String) iter.next();

				// is this a force?
				if (Category.getForces().contains(type))
				{
					forces.add(type);
				}
				else if (Category.getTypes().contains(type))
				{
					types.add(type);
				}
				else if (Category.getEnvironments().contains(type))
				{
					envs.add(type);
				}
			}
			
			String[] template = new String[]{""};
			if(forces.size() > 0)
				_myForce.setSelection((String[]) forces.toArray(template));
			if(types.size() > 0)
				_myType.setSelection((String[]) types.toArray(template));
			if(envs.size() > 0)
				_myEnvironment.setSelection((String[]) envs.toArray(template));
			
		}
		

		protected void setTypes(List holder, TargetType tt)
		{
			int[] forces = holder.getSelectionIndices();
			for (int thisI = 0; thisI < forces.length; thisI++)
			{
				String f = holder.getItem(thisI);
				tt.addTargetType(f);
			}
		}

		protected TargetType getResult()
		{
			TargetType tt = new TargetType();
			
			setTypes(_myForce, tt);
			setTypes(_myType, tt);
			setTypes(_myEnvironment, tt);
			
			return tt;
		}
		

		/**
		 * @return our list of values
		 */
		protected String[] getForces()
		{
			String[] res = new String[] { null };
			res = (String[]) Category.getForces().toArray(res);
			return res;
		}

		/**
		 * @return our list of values
		 */
		protected String[] getEnvironments()
		{
			String[] res = new String[] { null };
			res = (String[]) Category.getEnvironments().toArray(res);
			return res;
		}

		/**
		 * @return our list of values
		 */
		protected String[] getTypes()
		{
			String[] res = new String[] { null };
			res = (String[]) Category.getTypes().toArray(res);
			return res;
		}
		
		
	}
	

	private static class TargetTypeCellEditor extends DialogCellEditor
	{
		/**
		 * constructor - just pass on to parent
		 * 
		 * @param cellParent
		 */
		public TargetTypeCellEditor(Composite cellParent)
		{
			super(cellParent);
		}

		/**
		 * override operation triggered when button pressed. We should strictly be
		 * opening a new dialog, instead we're looking for a valid location on the
		 * clipboard. If one is there, we paste it.
		 * 
		 * @param cellEditorWindow
		 *          the parent control we belong to
		 * @return
		 */
		protected Object openDialogBox(Control cellEditorWindow)
		{
			TargetType res = null;
			
			TargetTypeDialog dialog = new TargetTypeDialog(null, cellEditorWindow.getShell());
			
			int response = dialog.open();
			
			if(response == org.eclipse.jface.dialogs.Dialog.OK)
			{
				res = dialog.getResult();
			}
			
			
			// ok - create our popup window
//			
//			LatLongPropertySource output = null;
//
//			// right, see what's on the clipboard
//			// right, copy the location to the clipboard
//			Clipboard clip = CorePlugin.getDefault().getClipboard();
//			Object val = clip.getContents(TextTransfer.getInstance());
//			if (val != null)
//			{
//				String txt = (String) val;
//				if (CorePlugin.isLocation(txt))
//				{
//					// cool, get the text
//					WorldLocation loc = CorePlugin.fromClipboard(txt);
//
//					// create the output value
//					output = new LatLongPropertySource(loc);
//				}
//				else
//				{
//					CorePlugin.showMessage("Paste location",
//							"Sorry the clipboard text is not in the right format." + "\nContents:"
//									+ txt);
//				}
//			}
//			else
//			{
//				CorePlugin.showMessage("Paste location",
//						"Sorry, there is no suitable text on the clipboard");
//			}
			
			return res;
		}

		/**
		 * Creates the button for this cell editor under the given parent control.
		 * <p>
		 * The default implementation of this framework method creates the button
		 * display on the right hand side of the dialog cell editor. Subclasses may
		 * extend or reimplement.
		 * </p>
		 * 
		 * @param parent
		 *          the parent control
		 * @return the new button control
		 */
		protected Button createButton(Composite parent)
		{
			Button result = super.createButton(parent);
			result.setText("...");
			return result;
		}
	}
}