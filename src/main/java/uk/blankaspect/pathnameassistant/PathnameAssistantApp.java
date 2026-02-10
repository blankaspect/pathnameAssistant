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

import java.nio.file.Path;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.collections.FXCollections;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;

import javafx.scene.Scene;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.build.BuildUtils;

import uk.blankaspect.common.cls.ClassUtils;

import uk.blankaspect.common.collection.ArraySet;

import uk.blankaspect.common.config.AppAuxDirectory;
import uk.blankaspect.common.config.AppConfig;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;
import uk.blankaspect.common.exception2.LocationException;

import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.logging.ErrorLogger;

import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.resource.ResourceProperties;
import uk.blankaspect.common.resource.ResourceUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.NotificationDialog;

import uk.blankaspect.ui.jfx.exec.ExecUtils;

import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.listview.SimpleTextListView;

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

	/** The name of the application when used as a key. */
	private static final	String	NAME_KEY	= StringUtils.firstCharToLowerCase(SHORT_NAME);

	/** The name of the file that contains the build properties of the application. */
	private static final	String	BUILD_PROPERTIES_FILENAME	= "build.properties";

	/** The filename of the CSS style sheet. */
	private static final	String	STYLE_SHEET_FILENAME	= NAME_KEY + "-%02d.css";

	/** The horizontal gap between adjacent controls. */
	private static final	double	CONTROL_H_GAP	= 6.0;

	/** The vertical gap between adjacent controls. */
	private static final	double	CONTROL_V_GAP	= 6.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(6.0, 8.0, 6.0, 8.0);

	/** The preferred width of the list view of locations. */
	private static final	double	LOCATIONS_LIST_VIEW_WIDTH	= 480.0;

	/** The preferred height of the list view of locations. */
	private static final	double	LOCATIONS_LIST_VIEW_HEIGHT	= 240.0;

	/** A map from system-property keys to the default values of the corresponding delays (in milliseconds) in the
		<i>WINDOW_SHOWN</i> event handler of the main window. */
	private static final	Map<String, Integer>	MAIN_WINDOW_DELAYS	= Map.of
	(
		SystemPropertyKey.MAIN_WINDOW_DELAY_SIZE,     50,
		SystemPropertyKey.MAIN_WINDOW_DELAY_LOCATION, 25,
		SystemPropertyKey.MAIN_WINDOW_DELAY_OPACITY,  25
	);

	/** The margins that are applied to the visual bounds of each screen when determining whether the saved location of
		the main window is within a screen. */
	private static final	Insets	SCREEN_MARGINS	= new Insets(0.0, 32.0, 32.0, 0.0);

	/** Miscellaneous strings. */
	private static final	String	CONFIG_ERROR_STR	= "Configuration error";
	private static final	String	FILE_STR			= "File";
	private static final	String	EDIT_STR			= "Edit";
	private static final	String	FORMAT_STR			= "Format";
	private static final	String	LOCATIONS_STR		= "Locations";

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
		String	MAIN_WINDOW_DELAY_LOCATION	= "mainWindowDelay.location";
		String	MAIN_WINDOW_DELAY_OPACITY	= "mainWindowDelay.opacity";
		String	MAIN_WINDOW_DELAY_SIZE		= "mainWindowDelay.size";
		String	SYSTEM_NAME					= "os.name";
		String	USE_STYLE_SHEET_FILE		= "useStyleSheetFile";
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	NO_AUXILIARY_DIRECTORY =
				"The location of the auxiliary directory could not be determined.";

		String	NO_LOCATIONS_ON_CLIPBOARD =
				"There are no file-system locations on the clipboard.";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	/** The single instance of this class. */
	private static	PathnameAssistantApp	instance;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The properties of the build of this application. */
	private	ResourceProperties			buildProperties;

	/** The string representation of the version of this application. */
	private	String						versionStr;

	/** The state of the main window. */
	private	WindowState					mainWindowState;

	/** A list of the file-system locations that are displayed in {@link #locationsListView}. */
	private	List<Path>					locations;

	/** The main window of this application. */
	private	Stage						primaryStage;

	/** The <i>edit</i> menu. */
	private	Menu						editMenu;

	/** The spinner for the pathname format. */
	private	CollectionSpinner<Format>	formatSpinner;

	/** The list view of file-system locations. */
	private	SimpleTextListView<Path>	locationsListView;

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

	/**
	 * Returns the delay (in milliseconds) that is defined the system property with the specified key.
	 *
	 * @param  key
	 *           the key of the system property.
	 * @return the delay (in milliseconds) that is defined the system property whose key is {@code key}, or a default
	 *         value if there is no such property or the property value is not a valid integer.
	 */

	private static int getDelay(
		String	key)
	{
		int delay = MAIN_WINDOW_DELAYS.get(key);
		String value = System.getProperty(key);
		if (value != null)
		{
			try
			{
				delay = Integer.parseInt(value);
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
		return delay;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public void init()
	{
		instance = this;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public void start(
		Stage	primaryStage)
	{
		// Make main window invisible until it is shown
		primaryStage.setOpacity(0.0);

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
		mainWindowState = new WindowState(false, true);
		locations = new ArraySet<>();
		this.primaryStage = primaryStage;

		// Read build properties and initialise version string
		try
		{
			buildProperties =
					new ResourceProperties(ResourceUtils.normalisedPathname(getClass(), BUILD_PROPERTIES_FILENAME));
			versionStr = BuildUtils.versionString(getClass(), buildProperties);
		}
		catch (LocationException e)
		{
			e.printStackTrace();
		}

		// Create container for local variables
		class Vars
		{
			Configuration	config;
			BaseException	configException;
		}
		Vars vars = new Vars();

		// Read configuration file and decode configuration
		try
		{
			// Initialise configuration
			vars.config = new Configuration();

			// Read and decode configuration
			if (!AppConfig.noConfigFile())
			{
				// Read configuration file
				vars.config.read();

				// Decode configuration
				decodeConfig(vars.config.getConfig());
			}
		}
		catch (BaseException e)
		{
			vars.configException = e;
		}

		// Get style manager
		StyleManager styleManager = StyleManager.INSTANCE;

		// Select theme from system property
		String themeId = System.getProperty(StyleManager.SystemPropertyKey.THEME);
		if (!StringUtils.isNullOrEmpty(themeId))
			styleManager.selectThemeOrDefault(themeId);

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
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(Region.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		controlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		controlPane.getColumnConstraints().add(column);

		// Initialise row index
		int row = 0;

		// Spinner: format
		formatSpinner = CollectionSpinner.leftRightH(HPos.CENTER, true, Format.class, Format.NATIVE, null, null);
		formatSpinner.itemProperty().addListener(observable -> locationsListView.refresh());
		controlPane.addRow(row++, new Label(FORMAT_STR), formatSpinner);

		// Label: locations
		Label locationsLabel = new Label(LOCATIONS_STR);
		GridPane.setValignment(locationsLabel, VPos.TOP);
		GridPane.setMargin(locationsLabel, new Insets(4.0, 0.0, 0.0, 0.0));

		// List view: locations
		locationsListView = new SimpleTextListView<>(location -> formatSpinner.getItem().locationToString(location));
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
			menu.getItems().addAll(
				Command.COPY_FILENAMES.newMenuItem(),
				Command.COPY_FILENAME_STEMS.newMenuItem(),
				Command.COPY_PATHNAMES.newMenuItem(),
				Command.COPY_PARENT_PATHNAME.newMenuItem(),
				Command.PASTE.newMenuItem(),
				new SeparatorMenuItem(),
				Command.DELETE_LOCATION.newMenuItem(),
				Command.CLEAR_LOCATIONS.newMenuItem(),
				new SeparatorMenuItem(),
				Command.SORT_LOCATIONS.newMenuItem()
			);
			updateMenuItems(menu.getItems());
			menu.show(primaryStage, event.getScreenX(), event.getScreenY());
			event.consume();
		});

		// Create scene
		Scene scene = new Scene(mainPane);

		// Add style sheet to scene
		styleManager.addStyleSheet(scene);

		// Handle 'drag over' events
		scene.setOnDragOver(event ->
		{
			// Accept drag if dragboard contains a file-system location
			if (event.getDragboard().hasFiles())
				event.acceptTransferModes(TransferMode.COPY);

			// Consume event
			event.consume();
		});

		// Handle 'drag dropped' events
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
		primaryStage.sizeToScene();

		// When main window is shown, set its size and location
		primaryStage.setOnShown(event ->
		{
			// Set size of main window after a delay
			ExecUtils.afterDelay(getDelay(SystemPropertyKey.MAIN_WINDOW_DELAY_SIZE), () ->
			{
				// Get size of window from saved state
				Dimension2D size = mainWindowState.getSize();

				// Set size of window
				if (size != null)
				{
					primaryStage.setWidth(size.getWidth());
					primaryStage.setHeight(size.getHeight());
				}

				// Set size and location of main window after a delay
				ExecUtils.afterDelay(getDelay(SystemPropertyKey.MAIN_WINDOW_DELAY_LOCATION), () ->
				{
					// Get location of window from saved state
					Point2D location = mainWindowState.getLocation();

					// Invalidate location if top centre of window is not within a screen
					double width = primaryStage.getWidth();
					if ((location != null) && !SceneUtils.isWithinScreen(location.getX() + 0.5 * width, location.getY(),
																		 SCREEN_MARGINS))
						location = null;

					// If there is no location, centre window within primary screen
					if (location == null)
						location = SceneUtils.centreInScreen(width, primaryStage.getHeight());

					// Set location of window
					primaryStage.setX(location.getX());
					primaryStage.setY(location.getY());

					// Perform remaining initialisation after a delay
					ExecUtils.afterDelay(getDelay(SystemPropertyKey.MAIN_WINDOW_DELAY_OPACITY), () ->
					{
						// Make window visible
						primaryStage.setOpacity(1.0);

						// Report any configuration error
						if (vars.configException != null)
							ErrorDialog.show(primaryStage, SHORT_NAME + " : " + CONFIG_ERROR_STR, vars.configException);
					});
				});
			});
		});

		// Write configuration file when main window is closed
		if (vars.config != null)
		{
			primaryStage.setOnHiding(event ->
			{
				// Update state of main window
				mainWindowState.restoreAndUpdate(primaryStage);

				// Write configuration
				if (vars.config.canWrite())
				{
					try
					{
						// Encode configuration
						encodeConfig(vars.config.getConfig());

						// Write configuration file
						vars.config.write();
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
		String themeId = StyleManager.INSTANCE.getThemeId();
		if (themeId != null)
			rootNode.addMap(PropertyKey.APPEARANCE).addString(PropertyKey.THEME, themeId);

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
		{
			String themeId = rootNode.getMapNode(key).getString(PropertyKey.THEME, StyleManager.DEFAULT_THEME_ID);
			StyleManager.INSTANCE.selectThemeOrDefault(themeId);
		}

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

		// Add items to 'file' menu
		fileMenu.getItems().add(Command.EXIT.newMenuItem());

		// Create menu: edit
		editMenu = new Menu(EDIT_STR);
		menuBar.getMenus().add(editMenu);

		// Add items to 'edit' menu
		editMenu.getItems().addAll(
			Command.COPY_FILENAMES.newMenuItem(),
			Command.COPY_FILENAME_STEMS.newMenuItem(),
			Command.COPY_PATHNAMES.newMenuItem(),
			Command.COPY_PARENT_PATHNAME.newMenuItem(),
			Command.PASTE.newMenuItem(),
			new SeparatorMenuItem(),
			Command.DELETE_LOCATION.newMenuItem(),
			Command.CLEAR_LOCATIONS.newMenuItem(),
			new SeparatorMenuItem(),
			Command.SORT_LOCATIONS.newMenuItem(),
			new SeparatorMenuItem(),
			Command.EDIT_PREFERENCES.newMenuItem()
		);

		// Enable/disable items of 'edit' menu when menu is displayed
		editMenu.setOnShowing(event -> updateEditMenu());

		// Enable/disable items of 'edit' menu
		updateEditMenu();

		// Return menu bar
		return menuBar;
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
			if (menuItem.getUserData() instanceof Command command)
			{
				menuItem.setDisable(switch (command)
				{
					case CLEAR_LOCATIONS,
						 COPY_FILENAMES,
						 COPY_FILENAME_STEMS,
						 COPY_PATHNAMES       -> locations.isEmpty();
					case COPY_PARENT_PATHNAME ->
					{
						Path location = locationsListView.getSelectionModel().getSelectedItem();
						yield (location == null) || (location.getParent() == null);
					}
					case DELETE_LOCATION      -> locationsListView.getSelectionModel().isEmpty();
					case SORT_LOCATIONS       -> (locations.size() < 2);
					default                   -> false;
				});
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
		locationsListView.setItems(FXCollections.observableArrayList(locations));
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

		// Update list view
		updateListView();

		// Update 'edit' menu
		updateEditMenu();
	}

	//------------------------------------------------------------------

	/**
	 * Fires an event to request the closure of the main window.  If the event is not consumed, the application is
	 * terminated.
	 */

	private void onExit()
	{
		primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	//------------------------------------------------------------------

	/**
	 * Copies the filename of each element of the list of locations to the system clipboard.  If there is more than one
	 * location, a line-feed character (U+000A) is appended to each filename.
	 */

	private void onCopyFilenames()
	{
		if (!locations.isEmpty())
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
				ErrorDialog.show(primaryStage, Command.COPY_FILENAMES.text, e);
			}
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
		if (!locations.isEmpty())
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
				ErrorDialog.show(primaryStage, Command.COPY_FILENAME_STEMS.text, e);
			}
		}
	}

	//------------------------------------------------------------------

	/**
	 * Copies the pathname of each element of the list of locations to the system clipboard.  A location is converted to
	 * a pathname according to the format that is selected in the spinner.  If there is more than one location, a
	 * line-feed character (U+000A) is appended to each pathname.
	 */

	private void onCopyPathnames()
	{
		if (!locations.isEmpty())
		{
			// Create text
			Format format = formatSpinner.getItem();
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
				ErrorDialog.show(primaryStage, Command.COPY_PATHNAMES.text, e);
			}
		}
	}

	//------------------------------------------------------------------

	/**
	 * Copies the pathname of the parent of the location that is selected in the list view to the system clipboard.  The
	 * location is converted to a pathname according to the format that is selected in the spinner.
	 */

	private void onCopyParentPathname()
	{
		Path location = locationsListView.getSelectionModel().getSelectedItem();
		if (location != null)
		{
			Path parent = location.getParent();
			if (parent != null)
			{
				try
				{
					ClipboardUtils.putTextThrow(formatSpinner.getItem().locationToString(parent));
				}
				catch (BaseException e)
				{
					ErrorDialog.show(primaryStage, Command.COPY_PARENT_PATHNAME.text, e);
				}
			}
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
			NotificationDialog.show(primaryStage, Command.PASTE.text, MessageIcon32.ALERT.get(),
									ErrorMsg.NO_LOCATIONS_ON_CLIPBOARD);
		}
		else
			appendLocations(locations);
	}

	//------------------------------------------------------------------

	/**
	 * Removes the selected location from the list of locations that is displayed in the list view.
	 */

	private void onDeleteLocation()
	{
		int index = locationsListView.getSelectionModel().getSelectedIndex();
		if (index >= 0)
		{
			// Remove item from list
			locations.remove(index);

			// Update list view
			updateListView();

			// Select item at old index
			if (!locations.isEmpty())
				locationsListView.getSelectionModel().select(Math.min(index, locations.size() - 1));

			// Update 'edit' menu
			updateEditMenu();
		}
	}

	//------------------------------------------------------------------

	/**
	 * Removes all elements from the list of locations that is displayed in the list view.
	 */

	private void onClearLocations()
	{
		if (!locations.isEmpty())
		{
			// Update instance variable
			locations.clear();

			// Update list view
			updateListView();

			// Update 'edit' menu
			updateEditMenu();
		}
	}

	//------------------------------------------------------------------

	/**
	 * Sorts the locations that are displayed in the list view lexicographically using the default comparator for the
	 * file system.  Sorting is terminated prematurely if locations from different file systems are compared.
	 */

	private void onSortLocations()
	{
		if (locations.size() > 1)
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
	}

	//------------------------------------------------------------------

	/**
	 * Displays a modal dialog in which the user preferences can be edited.
	 */

	private void onEditPreferences()
	{
		PreferencesDialog.show(primaryStage);
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
				return PathUtils.absString(location);
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
				return PathUtils.absStringStd(location);
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
				String pathname = PathUtils.absString(location);
				String userHome = SystemUtils.userHomeDirectoryPathname();
				if ((userHome != null) && pathname.startsWith(userHome))
					pathname = "~" + pathname.substring(userHome.length());
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
		 *          the text that will represent the format.
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


	// ENUMERATION: COMMAND


	/**
	 * This is an enumeration of commands that can be executed on the application.
	 */

	private enum Command
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * Terminate the application.
		 */
		EXIT
		(
			"Exit",
			null,
			PathnameAssistantApp.instance::onExit
		),

		/**
		 * Copy the filenames of items in the list of locations to the system clipboard.
		 */
		COPY_FILENAMES
		(
			"Copy filenames",
			new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
			PathnameAssistantApp.instance::onCopyFilenames
		),

		/**
		 * Copy the filename stems of items in the list of locations to the system clipboard.
		 */
		COPY_FILENAME_STEMS
		(
			"Copy filename stems",
			new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
			PathnameAssistantApp.instance::onCopyFilenameStems
		),

		/**
		 * Copy the pathnames of items in the list of locations to the system clipboard.
		 */
		COPY_PATHNAMES
		(
			"Copy pathnames",
			new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN),
			PathnameAssistantApp.instance::onCopyPathnames
		),

		/**
		 * Copy the pathname of the parent of the selected location to the system clipboard.
		 */
		COPY_PARENT_PATHNAME
		(
			"Copy parent pathname",
			null,
			PathnameAssistantApp.instance::onCopyParentPathname
		),

		/**
		 * Append file-system locations from the system clipboard to the list of locations.
		 */
		PASTE
		(
			"Paste",
			new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN),
			PathnameAssistantApp.instance::onPaste
		),

		/**
		 * Remove the selected item from the list of locations.
		 */
		DELETE_LOCATION
		(
			"Delete location",
			new KeyCodeCombination(KeyCode.DELETE),
			PathnameAssistantApp.instance::onDeleteLocation
		),

		/**
		 * Remove all items from the list of locations.
		 */
		CLEAR_LOCATIONS
		(
			"Clear locations",
			new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN),
			PathnameAssistantApp.instance::onClearLocations
		),

		/**
		 * Sort the list of locations lexicographically.
		 */
		SORT_LOCATIONS
		(
			"Sort locations",
			new KeyCodeCombination(KeyCode.F9),
			PathnameAssistantApp.instance::onSortLocations
		),

		/**
		 * Edit the user preferences.
		 */
		EDIT_PREFERENCES
		(
			"Preferences",
			null,
			PathnameAssistantApp.instance::onEditPreferences
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The text that represents this command. */
		private	String						text;

		/** The key combination that invokes this command. */
		private	KeyCombination				keyCombo;

		/** The handler for action events that invoke this command. */
		private	EventHandler<ActionEvent>	eventHandler;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a command.
		 *
		 * @param text
		 *          the text that will represent the command.
		 * @param keyCombo
		 *          the key combination that will invoke the command.
		 * @param action
		 *          the action that will be performed by the command.
		 */

		private Command(
			String			text,
			KeyCombination	keyCombo,
			Runnable		action)
		{
			// Initialise instance variables
			this.text = text;
			this.keyCombo = keyCombo;
			eventHandler = event -> action.run();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates and returns a new instance of a menu item for this command.
		 *
		 * @return a new instance of a menu item for this command.
		 */

		private MenuItem newMenuItem()
		{
			MenuItem menuItem = new MenuItem(text);
			menuItem.setUserData(this);
			if (keyCombo != null)
				menuItem.setAccelerator(keyCombo);
			menuItem.setOnAction(eventHandler);
			return menuItem;
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

			// Determine location of config file
			if (!noConfigFile())
			{
				// Get location of parent directory of config file
				AppAuxDirectory.Directory directory =
						AppAuxDirectory.getDirectory(NAME_KEY, getClass().getEnclosingClass());
				if (directory == null)
					throw new BaseException(ErrorMsg.NO_AUXILIARY_DIRECTORY);

				// Set parent directory of config file
				setDirectory(directory.location());
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
