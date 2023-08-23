/*====================================================================*\

PathnameAssistantApp.java

Class: pathname-assistant application.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.pathnameassistant;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;
import java.io.IOException;

import java.lang.invoke.MethodHandles;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.collections.FXCollections;

import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;
import javafx.stage.Window;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.build.BuildUtils;

import uk.blankaspect.common.cls.ClassUtils;

import uk.blankaspect.common.collection.ArraySet;

import uk.blankaspect.common.config.AppAuxDirectory;
import uk.blankaspect.common.config.AppConfig;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;
import uk.blankaspect.common.exception2.LocationException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.common.logging.ErrorLogger;

import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.resource.ResourceProperties;
import uk.blankaspect.common.resource.ResourceUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.NotificationDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;

import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.window.WindowState;

//----------------------------------------------------------------------


// CLASS: PATHNAME-ASSISTANT APPLICATION


public class PathnameAssistantApp
	extends Application
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The short name of the application. */
	private static final	String	SHORT_NAME	= "PathnameAssistant";

	/** The long name of the application. */
	private static final	String	LONG_NAME	= "Pathname assistant";

	/** The key with which the application is associated. */
	private static final	String	NAME_KEY	= StringUtils.firstCharToLowerCase(SHORT_NAME);

	/** The name of the file that contains the build properties of the application. */
	private static final	String	BUILD_PROPERTIES_FILENAME	= "build.properties";

	/** The filename of the CSS style sheet. */
	private static final	String	STYLE_SHEET_FILENAME	= NAME_KEY + "-%02d.css";

	/** The horizontal gap between adjacent controls. */
	private static final	double	CONTROL_V_GAP	= 6.0;

	/** The vertical gap between adjacent controls. */
	private static final	double	CONTROL_H_GAP	= 6.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(6.0, 8.0, 6.0, 8.0);

	/** The preferred width of the list view of locations. */
	private static final	double	LOCATIONS_LIST_VIEW_WIDTH	= 480.0;

	/** The preferred height of the list view of locations. */
	private static final	double	LOCATIONS_LIST_VIEW_HEIGHT	= 240.0;

	/** The margins that are applied to the visual bounds of each screen when determining whether the saved location of
		the main window is within a screen. */
	private static final	Insets	SCREEN_MARGINS	= new Insets(0.0, 32.0, 32.0, 0.0);

	/** Miscellaneous strings. */
	private static final	String	CONFIG_ERROR_STR		= "Configuration error";
	private static final	String	FILE_STR				= "File";
	private static final	String	EXIT_STR				= "Exit";
	private static final	String	EDIT_STR				= "Edit";
	private static final	String	COPY_FILENAMES_STR		= "Copy filenames";
	private static final	String	COPY_FILENAME_STEMS_STR	= "Copy filename stems";
	private static final	String	COPY_PATHNAMES_STR		= "Copy pathnames";
	private static final	String	PASTE_STR				= "Paste";
	private static final	String	CLEAR_LOCATIONS_STR		= "Clear locations";
	private static final	String	SORT_LOCATIONS_STR		= "Sort locations";
	private static final	String	PREFERENCES_STR			= "Preferences";
	private static final	String	FORMAT_STR				= "Format";
	private static final	String	LOCATIONS_STR			= "Locations";

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	APPEARANCE	= "appearance";
		String	MAIN_WINDOW	= "mainWindow";
		String	THEME		= "theme";
	}

	/** Keys of system properties. */
	private interface SystemPropertyKey
	{
		String	SYSTEM_NAME				= "os.name";
		String	USE_STYLE_SHEET_FILE	= "useStyleSheetFile";
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	NO_AUXILIARY_DIRECTORY		= "The location of the auxiliary directory could not be determined.";
		String	NO_LOCATIONS_ON_CLIPBOARD	= "There are no file-system locations on the clipboard.";
	}

	/** Requirements of menu items. **/
	private enum MenuItemRequirement
	{
		LOCATIONS,
		MULTIPLE_LOCATIONS
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The properties of the build of this application. */
	private	ResourceProperties	buildProperties;

	/** The string representation of the version of this application. */
	private	String				versionStr;

	/** The configuration of this application. */
	private	Configuration		configuration;

	/** User preferences. */
	private	Preferences			preferences;

	/** The state of the main window. */
	private	WindowState			mainWindowState;

	/** A list of the file-system locations that are displayed in the <i>locations</i> text area. */
	private	List<Path>			locations;

	/** The main window of this application. */
	private	Stage				primaryStage;

	/** The <i>edit</i> menu. */
	private	Menu				editMenu;

	/** The choice box for the pathname format. */
	private	ChoiceBox<Format>	formatChoiceBox;

	/** The list view of file-system locations. */
	private	ListView<String>	locationsListView;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of the application.
	 */

	public PathnameAssistantApp()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * This is the main method of the application.
	 *
	 * @param args
	 *          the command-line arguments of the application.
	 */

	public static void main(
		String[]	args)
	{
		launch(args);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	public void start(
		Stage	primaryStage)
	{
		// Log stack trace of uncaught exception
		if (ClassUtils.isFromJar(getClass()))
		{
			Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
			{
				try
				{
					ErrorLogger.INSTANCE.write(exception);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			});
		}

		// Initialise instance variables
		preferences = new Preferences();
		mainWindowState = new WindowState(false, true);
		locations = new ArraySet<>();
		this.primaryStage = primaryStage;

		// Read build properties and initialise version string
		try
		{
			buildProperties = new ResourceProperties(ResourceUtils.normalisedPathname(getClass(), BUILD_PROPERTIES_FILENAME));
			versionStr = BuildUtils.versionString(getClass(), buildProperties);
		}
		catch (LocationException e)
		{
			e.printStackTrace();
		}

		// Read configuration file and decode configuration
		BaseException configException = null;
		try
		{
			// Initialise configuration
			configuration = new Configuration();

			// Read configuration file
			configuration.read();

			// Decode configuration
			decodeConfig(configuration.getConfig());
		}
		catch (BaseException e)
		{
			configException = e;
		}

		// Get style manager
		StyleManager styleManager = StyleManager.INSTANCE;

		// Select theme
		String themeId = System.getProperty(StyleManager.SystemPropertyKey.THEME);
		if (StringUtils.isNullOrEmpty(themeId))
			themeId = preferences.themeId;
		if (themeId != null)
			styleManager.selectTheme(themeId);

		// Set ID and style-sheet filename on style manager
		if (Boolean.getBoolean(SystemPropertyKey.USE_STYLE_SHEET_FILE))
		{
			styleManager.setId(getClass().getSimpleName());
			styleManager.setStyleSheetFilename(STYLE_SHEET_FILENAME);
		}

		// Create control pane
		GridPane controlPane = new GridPane();
		controlPane.setVgap(CONTROL_V_GAP);
		controlPane.setHgap(CONTROL_H_GAP);
		controlPane.setPadding(CONTROL_PANE_PADDING);
		VBox.setVgrow(controlPane, Priority.ALWAYS);

		// Set column constraints
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setMinWidth(GridPane.USE_PREF_SIZE);
		column1.setHalignment(HPos.RIGHT);
		controlPane.getColumnConstraints().add(column1);

		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHalignment(HPos.LEFT);
		controlPane.getColumnConstraints().add(column2);

		// Initialise row index
		int row = 0;

		// Choice box: format
		formatChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Format.values()));
		formatChoiceBox.setValue(Format.NATIVE);
		formatChoiceBox.valueProperty().addListener(observable -> updateListView());
		controlPane.addRow(row++, new Label(FORMAT_STR), formatChoiceBox);

		// Label: locations
		Label locationsLabel = new Label(LOCATIONS_STR);
		GridPane.setValignment(locationsLabel, VPos.TOP);
		GridPane.setMargin(locationsLabel, new Insets(4.0, 0.0, 0.0, 0.0));

		// List view: locations
		locationsListView = new ListView<>();
		locationsListView.setPrefSize(LOCATIONS_LIST_VIEW_WIDTH, LOCATIONS_LIST_VIEW_HEIGHT);
		GridPane.setHgrow(locationsListView, Priority.ALWAYS);
		GridPane.setVgrow(locationsListView, Priority.ALWAYS);
		controlPane.addRow(row++, locationsLabel, locationsListView);

		// Create main pane
		VBox mainPane = new VBox(createMenuBar(), controlPane);
		mainPane.setAlignment(Pos.TOP_CENTER);
		mainPane.setOnContextMenuRequested(event ->
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(getEditMenuItems());
			menu.setOnShowing(event0 -> updateMenuItems(menu.getItems()));
			menu.show(primaryStage, event.getScreenX(), event.getScreenY());
			event.consume();
		});

		// Create scene
		Scene scene = new Scene(mainPane);

		// Add style sheet to scene
		styleManager.addStyleSheet(scene);

		// Handle DRAG_OVER drag events
		scene.setOnDragOver(event ->
		{
			// Accept drag if dragboard contains a file-system location
			if (event.getDragboard().hasFiles())
				event.acceptTransferModes(TransferMode.COPY);

			// Consume event
			event.consume();
		});

		// Handle DRAG_DROPPED drag events
		scene.setOnDragDropped(event ->
		{
			// Get file-system locations from dragboard
			List<Path> locations = ClipboardUtils.matchingLocations(event.getDragboard(), null);

			// Indicate that drag-and-drop is complete
			event.setDropCompleted(true);

			// Append locations
			if (!locations.isEmpty())
			{
				// Request focus on main window
				primaryStage.requestFocus();

				// Append locations to list
				Platform.runLater(() ->
				{
					// Append locations to list
					appendLocations(locations);

					// Request focus on list view
					locationsListView.requestFocus();
				});
			}

			// Consume event
			event.consume();
		});

		// Set properties of main window
		primaryStage.setTitle(LONG_NAME + " " + versionStr);
		primaryStage.getIcons().addAll(Images.APP_ICON_IMAGES);

		// Set scene on main window
		primaryStage.setScene(scene);

		// Set location and size of main window when it is opening
		primaryStage.setOnShowing(event ->
		{
			// Set location of window
			Point2D location = mainWindowState.getLocation();
			if (location != null)
			{
				primaryStage.setX(location.getX());
				primaryStage.setY(location.getY());
			}

			// Set size of window
			Dimension2D size = mainWindowState.getSize();
			if (size == null)
				primaryStage.sizeToScene();
			else
			{
				primaryStage.setWidth(size.getWidth());
				primaryStage.setHeight(size.getHeight());
			}
		});

		// Set location of main window after it is shown
		primaryStage.setOnShown(event ->
		{
			// Get location of window
			Point2D location = mainWindowState.getLocation();

			// Invalidate location if top centre of window is not within a screen
			double width = primaryStage.getWidth();
			if ((location != null)
					&& !SceneUtils.isWithinScreen(location.getX() + 0.5 * width, location.getY(), SCREEN_MARGINS))
				location = null;

			// If there is no location, centre window within primary screen
			if (location == null)
			{
				location = SceneUtils.centreInScreen(width, primaryStage.getHeight());
				primaryStage.setX(location.getX());
				primaryStage.setY(location.getY());
			}
		});

		// Write configuration file when main window is closed
		if (configuration != null)
		{
			primaryStage.setOnHiding(event ->
			{
				// Update state of main window
				mainWindowState.restoreAndUpdate(primaryStage);

				// Write configuration
				if (configuration.canWrite())
				{
					try
					{
						// Encode configuration
						encodeConfig(configuration.getConfig());

						// Write configuration file
						configuration.write();
					}
					catch (FileException e)
					{
						ErrorDialog.show(primaryStage, SHORT_NAME + " : " + CONFIG_ERROR_STR, e);
					}
				}
			});
		}

		// Display main window
		primaryStage.show();

		// Report any configuration error
		if (configException != null)
			ErrorDialog.show(primaryStage, SHORT_NAME + " : " + CONFIG_ERROR_STR, configException);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Encodes the configuration of this application to the specified root node.
	 *
	 * @param rootNode
	 *          the root node of the configuration.
	 */

	private void encodeConfig(
		MapNode	rootNode)
	{
		// Clear properties
		rootNode.clear();

		// Encode theme ID
		rootNode.addMap(PropertyKey.APPEARANCE).addString(PropertyKey.THEME, preferences.themeId);

		// Encode state of main window
		MapNode windowStateNode = mainWindowState.encodeTree();
		if (!windowStateNode.isEmpty())
			rootNode.add(PropertyKey.MAIN_WINDOW, windowStateNode);
	}

	//------------------------------------------------------------------

	/**
	 * Decodes the configuration of this application from the specified root node.
	 *
	 * @param rootNode
	 *          the root node of the configuration.
	 */

	private void decodeConfig(
		MapNode	rootNode)
	{
		// Decode theme ID
		String key = PropertyKey.APPEARANCE;
		if (rootNode.hasMap(key))
			preferences.themeId = rootNode.getMapNode(key).getString(PropertyKey.THEME, StyleManager.DEFAULT_THEME_ID);

		// Decode state of main window
		key = PropertyKey.MAIN_WINDOW;
		if (rootNode.hasMap(key))
			mainWindowState.decodeTree(rootNode.getMapNode(key));
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a menu bar for the main window.
	 *
	 * @return a menu bar for the main window.
	 */

	private MenuBar createMenuBar()
	{
		// Create menu bar
		MenuBar menuBar = new MenuBar();
		menuBar.setPadding(Insets.EMPTY);

		// Create menu: file
		Menu fileMenu = new Menu(FILE_STR);
		menuBar.getMenus().add(fileMenu);

		// Add menu item: exit
		MenuItem menuItem = new MenuItem(EXIT_STR);
		menuItem.setOnAction(event -> Platform.exit());
		fileMenu.getItems().add(menuItem);

		// Create menu: edit
		editMenu = new Menu(EDIT_STR);
		menuBar.getMenus().add(editMenu);

		// Get items for 'edit' menu
		List<MenuItem> menuItems = getEditMenuItems();

		// Add separator
		menuItems.add(new SeparatorMenuItem());

		// Add menu item: preferences
		menuItem = new MenuItem(PREFERENCES_STR);
		menuItem.setOnAction(event -> onEditPreferences());
		menuItems.add(menuItem);

		// Add menu items
		editMenu.getItems().addAll(menuItems);

		// Enable/disable items of 'edit' menu when menu is displayed
		editMenu.setOnShowing(event -> updateEditMenu());

		// Update 'edit' menu
		updateEditMenu();

		// Return menu bar
		return menuBar;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of menu items that appear in the <i>Edit</i> menu of the main window and the context menu of the
	 * list view.
	 *
	 * @return a list of menu items.
	 */

	private List<MenuItem> getEditMenuItems()
	{
		// Initialise list of menu items
		List<MenuItem> menuItems = new ArrayList<>();

		// Add menu item: copy filenames
		MenuItem menuItem = new MenuItem(COPY_FILENAMES_STR);
		menuItem.setUserData(MenuItemRequirement.LOCATIONS);
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onCopyFilenames());
		menuItems.add(menuItem);

		// Add menu item: copy filename stems
		menuItem = new MenuItem(COPY_FILENAME_STEMS_STR);
		menuItem.setUserData(MenuItemRequirement.LOCATIONS);
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onCopyFilenameStems());
		menuItems.add(menuItem);

		// Add menu item: copy pathnames
		menuItem = new MenuItem(COPY_PATHNAMES_STR);
		menuItem.setUserData(MenuItemRequirement.LOCATIONS);
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onCopyPathnames());
		menuItems.add(menuItem);

		// Add menu item: paste
		menuItem = new MenuItem(PASTE_STR);
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onPaste());
		menuItems.add(menuItem);

		// Add separator
		menuItems.add(new SeparatorMenuItem());

		// Add menu item: clear locations
		menuItem = new MenuItem(CLEAR_LOCATIONS_STR);
		menuItem.setUserData(MenuItemRequirement.LOCATIONS);
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onClearLocations());
		menuItems.add(menuItem);

		// Add separator
		menuItems.add(new SeparatorMenuItem());

		// Add menu item: sort locations
		menuItem = new MenuItem(SORT_LOCATIONS_STR);
		menuItem.setUserData(MenuItemRequirement.MULTIPLE_LOCATIONS);
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.F9));
		menuItem.setOnAction(event -> onSortLocations());
		menuItems.add(menuItem);

		// Return menu items
		return menuItems;
	}

	//------------------------------------------------------------------

	/**
	 * Enables or disables the specified menu items according to the user data of each item.
	 *
	 * @param menuItems
	 *          the menu items that will be updated.
	 */

	private void updateMenuItems(
		Iterable<? extends MenuItem>	menuItems)
	{
		for (MenuItem menuItem : menuItems)
		{
			if (menuItem.getUserData() instanceof MenuItemRequirement menuItemReq)
			{
				switch (menuItemReq)
				{
					case LOCATIONS:
						menuItem.setDisable(locations.isEmpty());
						break;

					case MULTIPLE_LOCATIONS:
						menuItem.setDisable(locations.size() < 2);
						break;
				}
			}
		}
	}

	//------------------------------------------------------------------

	/**
	 * Enables or disables the items of the <i>edit</i> menu.
	 */

	private void updateEditMenu()
	{
		updateMenuItems(editMenu.getItems());
	}

	//------------------------------------------------------------------

	/**
	 * Updates the pathnames that are displayed in the list view.
	 */

	private void updateListView()
	{
		Format format = formatChoiceBox.getValue();
		locationsListView.setItems(FXCollections.observableArrayList(
				locations.stream().map(location -> format.locationToString(location)).toList()));
	}

	//------------------------------------------------------------------

	/**
	 * Appends the specified file-system locations to the list of locations that is displayed in the list view.
	 *
	 * @param locations
	 *          the locations that will be appended to the list that is displayed in the list view.
	 */

	private void appendLocations(
		Collection<Path>	locations)
	{
		// Update instance variable
		this.locations.addAll(locations);

		// Update 'edit' menu
		updateEditMenu();

		// Update list view
		updateListView();
	}

	//------------------------------------------------------------------

	/**
	 * Copies the filename of each element of the list of locations to the system clipboard.  If there is more than one
	 * location, a line-feed character (U+000A) is appended to each filename.
	 */

	private void onCopyFilenames()
	{
		// Create text
		StringBuilder buffer = new StringBuilder(locations.size() * 32);
		for (Path location : locations)
		{
			buffer.append(location.getFileName());

			if (locations.size() > 1)
				buffer.append('\n');
		}

		// Put text on clipboard
		try
		{
			ClipboardUtils.putTextThrow(buffer.toString());
		}
		catch (BaseException e)
		{
			ErrorDialog.show(primaryStage, COPY_FILENAMES_STR, e);
		}
	}

	//------------------------------------------------------------------

	/**
	 * Copies the filename stem of each element of the list of locations to the system clipboard.  The stem of a
	 * filename is the substring from the start of the filename to the first occurrence of a dot, '.', but not including
	 * the dot.  If the filename does not contain a dot, the stem is the entire filename.  If there is more than one
	 * location, a line-feed character (U+000A) is appended to each filename stem.
	 */

	private void onCopyFilenameStems()
	{
		// Create text
		StringBuilder buffer = new StringBuilder(locations.size() * 32);
		for (Path location : locations)
		{
			String filename = location.getFileName().toString();
			int index = filename.indexOf('.');
			if (index > 1)
				filename = filename.substring(0, index);
			buffer.append(filename);

			if (locations.size() > 1)
				buffer.append('\n');
		}

		// Put text on clipboard
		try
		{
			ClipboardUtils.putTextThrow(buffer.toString());
		}
		catch (BaseException e)
		{
			ErrorDialog.show(primaryStage, COPY_FILENAME_STEMS_STR, e);
		}
	}

	//------------------------------------------------------------------

	/**
	 * Copies the pathname of each element of the list of locations to the system clipboard.  A location is converted to
	 * a pathname according to the format that is selected in the choice box.  If there is more than one location, a
	 * line-feed character (U+000A) is appended to each pathname.
	 */

	private void onCopyPathnames()
	{
		// Create text
		Format format = formatChoiceBox.getValue();
		StringBuilder buffer = new StringBuilder(locations.size() * 128);
		for (Path location : locations)
		{
			buffer.append(format.locationToString(location));

			if (locations.size() > 1)
				buffer.append('\n');
		}

		// Put text on clipboard
		try
		{
			ClipboardUtils.putTextThrow(buffer.toString());
		}
		catch (BaseException e)
		{
			ErrorDialog.show(primaryStage, COPY_PATHNAMES_STR, e);
		}
	}

	//------------------------------------------------------------------

	/**
	 * Appends any file-system locations that are on the system clipboard to the list of locations that is displayed in
	 * the list view.
	 */

	private void onPaste()
	{
		List<Path> locations = ClipboardUtils.locations();
		if (locations.isEmpty())
		{
			NotificationDialog.show(primaryStage, PASTE_STR, MessageIcon32.ALERT.get(),
									ErrorMsg.NO_LOCATIONS_ON_CLIPBOARD);
		}
		else
			appendLocations(locations);
	}

	//------------------------------------------------------------------

	/**
	 * Removes all elements from the list of locations that is displayed in the list view.
	 */

	private void onClearLocations()
	{
		// Update instance variable
		locations.clear();

		// Update 'edit' menu
		updateEditMenu();

		// Update list view
		updateListView();
	}

	//------------------------------------------------------------------

	/**
	 * Sorts the locations that are displayed in the list view lexicographically using the default comparator for the
	 * file system.  Sorting is terminated prematurely if locations from different file systems are compared.
	 */

	private void onSortLocations()
	{
		// Sort locations using default comparator for the associated file system
		try
		{
			locations.sort(null);
		}
		catch (ClassCastException e)
		{
			// ignore
		}

		// Update list view
		updateListView();
	}

	//------------------------------------------------------------------

	/**
	 * Displays a modal dialog in which the user preferences of the application may be edited.
	 */

	private void onEditPreferences()
	{
		Preferences result = new PreferencesDialog(primaryStage, PREFERENCES_STR).showDialog();
		if (result != null)
		{
			// Update instance variable
			preferences = result;

			// Apply theme
			StyleManager styleManager = StyleManager.INSTANCE;
			String themeId = preferences.themeId;
			if ((themeId != null) && !themeId.equals(styleManager.getThemeId()))
			{
				// Update theme
				styleManager.selectTheme(themeId);

				// Reapply style sheet to the scenes of all JavaFX windows
				styleManager.reapplyStylesheet();
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: PATHNAME FORMAT


	/**
	 * This is an enumeration of the available formats of a pathname of a file-system location.
	 */

	private enum Format
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * Adjacent elements of a pathname are separated with a {@linkplain File#separatorChar platform-dependent
		 * character} ('/' for Unix-like platforms, '\' for Windows).
		 */

		NATIVE
		(
			"Native"
		)
		{
			@Override
			protected String locationToString(
				Path	location)
			{
				return location.toAbsolutePath().toString();
			}
		},

		/**
		 * Adjacent elements of a pathname are separated with a '/'.
		 */

		UNIX_SEPARATOR
		(
			"Unix separator"
		)
		{
			@Override
			protected String locationToString(
				Path	location)
			{
				return location.toAbsolutePath().toString().replace(File.separatorChar, '/');
			}
		},

		/**
		 * Adjacent elements of a pathname are separated with a '/'.  If the location is the user's home directory or a
		 * location below it, the pathname of the user's home directory is replaced by '~'.
		 */

		REDUCED_UNIX
		(
			"Reduced Unix"
		)
		{
			@Override
			protected String locationToString(
				Path	location)
			{
				String pathname = location.toAbsolutePath().toString();
				String userHome = SystemUtils.getUserHomePathname();
				if ((userHome != null) && pathname.startsWith(userHome))
					pathname = PathnameUtils.USER_HOME_PREFIX + pathname.substring(userHome.length());
				return pathname.replace(File.separatorChar, '/');
			}
		},

		/**
		 * The file-system location is represented by a file-scheme URI.
		 */

		URI
		(
			"URI"
		)
		{
			@Override
			protected String locationToString(
				Path	location)
			{
				return location.toUri().toASCIIString();
			}
		};

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The text that represents this format. */
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a pathname format.
		 *
		 * @param text
		 *          the text that will represent this format.
		 */

		private Format(
			String	text)
		{
			// Initialise instance variables
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns a string representation of the specified file-system location.
		 *
		 * @param  location
		 *           the location for which a string representation is desired.
		 * @return a string representation of {@code location}.
		 */

		protected abstract String locationToString(
			Path	location);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public String toString()
		{
			String str = text;
			if (this == NATIVE)
			{
				String osName = System.getProperty(SystemPropertyKey.SYSTEM_NAME);
				if (osName != null)
					str += " (" + osName + ")";
			}
			return str;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CONFIGURATION


	/**
	 * This class implements the configuration of the application.
	 */

	private static class Configuration
		extends AppConfig
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The identifier of a configuration file. */
		private static final	String	ID	= "R9NVFEHFLGHWCKJ60K6N8C8J1";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of the configuration of the application.
		 *
		 * @throws BaseException
		 *           if the configuration directory could not be determined.
		 */

		private Configuration()
			throws BaseException
		{
			// Call superclass constructor
			super(ID, NAME_KEY, SHORT_NAME, LONG_NAME);

			// Get location of parent directory of config file
			AppAuxDirectory.Directory directory = AppAuxDirectory.getDirectory(NAME_KEY, PathnameAssistantApp.class);
			if (directory == null)
				throw new BaseException(ErrorMsg.NO_AUXILIARY_DIRECTORY);

			// Set parent directory of config file
			setDirectory(directory.location());
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PREFERENCES


	/**
	 * This class implements a set of user preferences.
	 */

	private static class Preferences
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The identifier of the theme. */
		private	String	themeId;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a default set of user preferences.
		 */

		private Preferences()
		{
			// Initialise instance variables
			themeId = StyleManager.DEFAULT_THEME_ID;
		}

		//--------------------------------------------------------------

		/**
		 * Creates a new instance of a set of user preferences with the specified values.
		 *
		 * @param themeId
		 *          the identifier of the theme.
		 */

		private Preferences(
			String	themeId)
		{
			// Initialise instance variables
			this.themeId = themeId;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PREFERENCES DIALOG


	/**
	 * This class implements a modal dialog in which the user preferences of the application may be edited.
	 */

	private static class PreferencesDialog
		extends SimpleModalDialog<Preferences>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The horizontal gap between adjacent components of the control pane. */
		private static final	double	CONTROL_PANE_H_GAP	= 6.0;

		/** The padding around the control pane. */
		private static final	Insets	CONTROL_PANE_PADDING	= new Insets(6.0, 12.0, 6.0, 12.0);

		/** Miscellaneous strings. */
		private static final	String	THEME_STR	= "Theme";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The result of this dialog. */
		private	Preferences	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a modal dialog in which the user preferences of the application may be edited.
		 *
		 * @param owner
		 *          the window that will be the owner of this dialog, or {@code null} if the dialog has no owner.
		 * @param title
		 *          the title of the dialog.
		 */

		private PreferencesDialog(
			Window	owner,
			String	title)
		{
			// Call superclass constructor
			super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), null, title);

			// Create procedure to select theme
			StyleManager styleManager = StyleManager.INSTANCE;
			IProcedure1<String> selectTheme = id ->
			{
				// Update theme
				styleManager.selectTheme(id);

				// Reapply style sheet to the scenes of all JavaFX windows
				styleManager.reapplyStylesheet();
			};

			// Create spinner: theme
			String themeId = styleManager.getThemeId();
			CollectionSpinner<String> themeSpinner =
					CollectionSpinner.leftRightH(HPos.CENTER, true, styleManager.getThemeIds(), themeId, null,
												 id -> styleManager.findTheme(id).getName());
			themeSpinner.itemProperty().addListener((observable, oldId, id) -> selectTheme.invoke(id));

			// Create control pane
			HBox controlPane = new HBox(CONTROL_PANE_H_GAP, new Label(THEME_STR), themeSpinner);
			controlPane.setMaxWidth(HBox.USE_PREF_SIZE);
			controlPane.setAlignment(Pos.CENTER_LEFT);
			controlPane.setPadding(CONTROL_PANE_PADDING);

			// Add control pane to content pane
			addContent(controlPane);

			// Create button: OK
			Button okButton = new Button(OK_STR);
			okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			okButton.setOnAction(event ->
			{
				// Get result
				result = new Preferences(themeSpinner.getItem());

				// Close dialog
				requestClose();
			});
			addButton(okButton, HPos.RIGHT);

			// Create button: cancel
			Button cancelButton = new Button(CANCEL_STR);
			cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			cancelButton.setOnAction(event -> requestClose());
			addButton(cancelButton, HPos.RIGHT);

			// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
			setKeyFireButton(cancelButton, okButton);

			// When window is closed, restore old theme if dialog was not accepted
			setOnHiding(event ->
			{
				if ((result == null) && (themeId != null) && !themeId.equals(styleManager.getThemeId()))
					selectTheme.invoke(themeId);
			});

			// Apply new style sheet to scene
			applyStyleSheet();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		protected Preferences getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
