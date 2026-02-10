/*====================================================================*\

PreferencesDialog.java

Class: preferences dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.pathnameassistant;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.Objects;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import javafx.stage.Window;

import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.label.Labels;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;

import uk.blankaspect.ui.jfx.style.StyleManager;

//----------------------------------------------------------------------


// CLASS: PREFERENCES DIALOG


/**
 * This class implements a modal dialog in which the user preferences of the application may be edited.
 */

class PreferencesDialog
	extends SimpleModalDialog<Boolean>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent components of the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(6.0, 12.0, 6.0, 12.0);

	/** Miscellaneous strings. */
	private static final	String	PREFERENCES_STR	= "Preferences";
	private static final	String	THEME_STR		= "Theme";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** Flag: if {@code true}, this dialog was accepted. */
	private	boolean	accepted;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a modal dialog in which the user preferences of the application may be edited.
	 *
	 * @param owner
	 *          the window that will be the owner of this dialog, or {@code null} if the dialog has no owner.
	 */

	private PreferencesDialog(
		Window	owner)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), null, PREFERENCES_STR);

		// Create procedure to select theme
		StyleManager styleManager = StyleManager.INSTANCE;
		IProcedure1<String> selectTheme = id ->
		{
			if (id != null)
			{
				// Update theme
				styleManager.selectTheme(id);

				// Reapply style sheet to the scenes of all JavaFX windows
				styleManager.reapplyStylesheet();
			}
		};

		// Create spinner: theme
		String themeId = styleManager.getThemeId();
		CollectionSpinner<String> themeSpinner =
				CollectionSpinner.leftRightH(HPos.CENTER, true, styleManager.getThemeIds(), themeId, null,
											 id -> styleManager.findTheme(id).name());
		themeSpinner.itemProperty().addListener((observable, oldId, id) -> selectTheme.invoke(id));

		// Create control pane
		HBox controlPane = new HBox(CONTROL_PANE_H_GAP, Labels.hNoShrink(THEME_STR), themeSpinner);
		controlPane.setMaxWidth(Region.USE_PREF_SIZE);
		controlPane.setAlignment(Pos.CENTER_LEFT);
		controlPane.setPadding(CONTROL_PANE_PADDING);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: OK
		Button okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Indicate that dialog was accepted
			accepted = true;

			// Close dialog
			requestClose();
		});
		addButton(okButton, HPos.RIGHT);

		// Create button: cancel
		Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelButton, okButton);

		// When window is closed, restore old theme if dialog was not accepted
		setOnHiding(event ->
		{
			if (!accepted && !Objects.equals(themeId, styleManager.getThemeId()))
				selectTheme.invoke(themeId);
		});

		// Apply new style sheet to scene
		applyStyleSheet();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void show(
		Window	owner)
	{
		new PreferencesDialog(owner).showDialog();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected Boolean getResult()
	{
		return accepted;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
