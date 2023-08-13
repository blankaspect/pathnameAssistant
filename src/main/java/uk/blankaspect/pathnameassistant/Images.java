/*====================================================================*\

Images.java

Class: images.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.pathnameassistant;

//----------------------------------------------------------------------


// IMPORTS


import java.util.List;

import javafx.scene.image.Image;

import uk.blankaspect.ui.jfx.image.ImageCache;

//----------------------------------------------------------------------


// CLASS: IMAGES


/**
 * This class provides images for the icons that represent the application.
 */

public class Images
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** A list of images for the icons that represent the application. */
	public static final		List<Image>	APP_ICON_IMAGES;

	/** The directory that contains image files. */
	private static final	String	IMAGE_DIRECTORY	= "images/";

	/** Filename stems of images that are used in this class. */
	private interface ImageFilename
	{
		String	APP_16	= "app-16x16";
		String	APP_32	= "app-32x32";
		String	APP_48	= "app-48x48";
		String	APP_256	= "app-256x256";
	}

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register directory containing images that represent the application
		ImageCache.addDirectory(Images.class, IMAGE_DIRECTORY);

		// Initialise images that represent the application
		APP_ICON_IMAGES = List.of
		(
			ImageCache.getImage(ImageFilename.APP_16),
			ImageCache.getImage(ImageFilename.APP_32),
			ImageCache.getImage(ImageFilename.APP_48),
			ImageCache.getImage(ImageFilename.APP_256)
		);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private	Images()
	{
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
