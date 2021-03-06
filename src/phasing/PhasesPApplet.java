package phasing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arb.soundcipher.SCScore;
import controlP5.Button;
import controlP5.ControlP5;
import controlP5.Controller;
import controlp5.Util;
import geom.Polygon;
import geom.Rect;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.data.JSONObject;
import processing.event.MouseEvent;
import screens.Editor;
import screens.HelpScreen;
import screens.PhraseRepository;
import screens.Presenter;
import screens.Screen;
import util.NameGenerator;

/**
 * 
 * @author James Morrow
 *
 */
public class PhasesPApplet extends PApplet {
    public final static int MIN_OCTAVE = 4;
    private final static boolean unlockEverything = false;

    // time
    private final static float minutesPerMillisecond = 1f / 60000f;

    // phrase picture name generator
    public static NameGenerator phrasePictureNameGenerator;

    // all music variables
    public final static String[] roots = new String[] { "A", "A#/Bb", "B", "C", "C#/Db", "D", "D#/Eb", "E", "F",
            "F#/Gb", "G", "G#/Ab" };
    public final static ArrayList<String> scaleTypes = new ArrayList<String>();
    private static Map<String, ScaleSet> scaleSets = new HashMap<String, ScaleSet>();

    // active music variables
    public Phrase currentPhrase;
    public PhrasePicture currentPhrasePicture;
    public Scale currentScale;
    private ArrayList<PhrasePicture> phrasePictures;

    public final static float DEFAULT_BPM_1 = 60;
    public final static float DEFAULT_BPM_DIFFERENCE = 0.5f;
    private float bpm1 = DEFAULT_BPM_1;
    private float bpms1 = bpm1 / 60000f;
    private float bpm2 = bpm1 + DEFAULT_BPM_DIFFERENCE;
    private float bpms2 = bpm2 / 60000f;

    // screens
    private Presenter presenter;
    private Editor editor;
    private PhraseRepository phraseRepo;
    private HelpScreen help;
    private boolean presenterEntered = false;
    private Screen prevScreen, currentScreen;

    // visual variables
    private static ColorScheme colorScheme;
    public static PFont pfont12, pfont14, pfont18, pfont24, pfont64, musicFont;

    // controlp5
    private ControlP5 cp5;
    private Button presentButton, composeButton, loadButton, helpButton;
    private static final int PRESENTER_BUTTON_X2 = 135;
    private static final int HELP_BUTTON_X2 = 240;
    private boolean helpOn;
    public final static int CONTROLLER_DX = 15;

    // screen size
    private int prevWidth, prevHeight;
    private static boolean initialWindowSizeGiven = false;
    private static int initialWidthSize = 800, initialHeightSize = 600; // default
                                                                        // window
                                                                        // size
                                                                        // if
                                                                        // given
                                                                        // no
                                                                        // command
                                                                        // line
                                                                        // args

    // save folder location
    public String saveFolderPath;
    public String dataFolderPath;

    // debug
    public static boolean showAnimationError = false;

    // player
    public PlayerInfo playerInfo;

    // cursor
    public static boolean hideCursor;

    /*****************
     ***** Setup *****
     *****************/

    /**
     * Sets the initial width and height of the window. But PhasesPApplet
     * reverts to its defaults if the given width is less than 800 or the given
     * height is less than 600.`
     */
    protected static void setInitialScreenSize(int width, int height) {
        if (width >= 800 && height >= 600) {
            initialWidthSize = width;
            initialHeightSize = height;
            initialWindowSizeGiven = true;
        }
    }

    /**
     * Sets up the size of the canvas/window
     */
    public void settings() {
        size(initialWidthSize, initialHeightSize, P2D);
        prevWidth = width;
        prevHeight = height;
    }

    /**
     * Initializes all of the class's static variables.
     */
    private void initStaticVariables() {
        // init font variables
        pfont12 = loadFont("DejaVuSans-12.vlw");
        pfont14 = loadFont("DejaVuSans-14.vlw");
        pfont18 = loadFont("DejaVuSans-18.vlw");
        pfont24 = loadFont("DejaVuSans-24.vlw");
        pfont64 = loadFont("DejaVuSans-64.vlw");
        musicFont = loadFont("MaestroWide-48.vlw");

        // init colors
        initColorScheme();
        // initSimpleColorScheme();

        // init folder paths
        saveFolderPath = this.sketchPath() + "/sav/";
        dataFolderPath = this.sketchPath() + "/data/";

        initPhrasePictures();

        // init name generator, excluding names given by phrasePictures
        phrasePictureNameGenerator = new NameGenerator(PhrasePicture.getNames(phrasePictures));
    }

    /**
     * Does the initial setup.
     */
    public void setup() {
        initStaticVariables();
        initPlayerInfo();
        initBPMData(playerInfo);
        surface.setResizable(true);
        initScales();
        if (currentPhrase == null) {
            initCurrentPhrase();
        }
        initCurrentScale(currentPhrase);
        initScreens();
        currentScreen = presenter;
        initCP5Objects(currentScreen);
        changeScreenTo(currentScreen);
        currentScreen.onEnter();
        if (hideCursor) {
            noCursor();
        }

        smooth();
        hint(this.DISABLE_OPTIMIZED_STROKE);
    }

    /**
     * Initializes the program-wide BPM (beats per minute) data.
     * 
     * @param playerInfo
     *            Indicates that the playerInfo variable should be initialized
     *            before this method is called.
     */
    private void initBPMData(PlayerInfo playerInfo) {
        setBPM1(playerInfo.bpm1);
        setBPM2(playerInfo.bpm1 + playerInfo.bpmDifference);
    }

    /**
     * Initializes the ControlP5 object and its controllers.
     * 
     * @param currentScreen
     *            Indicates that the method relies on currentScreen being
     *            already initialized.
     */
    private void initCP5Objects(Screen currentScreen) {
        if (cp5 != null) {
            cp5.dispose();
        }
        cp5 = new ControlP5(this);
        cp5.setAutoDraw(false);
        initChangeScreenButtons(currentScreen);
    }

    /**
     * Loads the phrase pictures.
     */
    private void initPhrasePictures() {
        boolean phrasePicturesLoaded = loadPhrasePictures();
        if (!phrasePicturesLoaded) {
            phrasePictures = new ArrayList<PhrasePicture>();
        }
    }

    /**
     * Initializes the screens.
     */
    private void initScreens() {
        presenter = new Presenter(this, showAnimationError);
        editor = new Editor(this);
        phraseRepo = new PhraseRepository(this);
        help = new HelpScreen(editor, this);
    }

    /**
     * Initializes the playerInfo.
     */
    private void initPlayerInfo() {
        if (unlockEverything) {
            playerInfo = new PlayerInfo(unlockEverything);
            savePlayerInfo();
        } else {
            boolean playerInfoLoaded = loadPlayerInfo();
            if (!playerInfoLoaded) {
                playerInfo = new PlayerInfo(unlockEverything);
                savePlayerInfo();
            }
        }
    }

    /**
     * Initializes the scales in general.
     * 
     */
    private void initScales() {
        loadScales();
    }

    /**
     * Initializes the current scale.
     * 
     * @param currentPhrase
     *            This must be initialized before this method can execute.
     */
    private void initCurrentScale(Phrase currentPhrase) {
        boolean currentScaleLoaded = setCurrentScale(currentPhrase.getScaleClassName(),
                currentPhrase.getScaleRootName());
        if (!currentScaleLoaded) {
            currentScale = getRandomScale();
        }
    }

    /**
     * Initializes the currentPhrase.
     */
    private void initCurrentPhrase() {
        boolean currentPhraseLoaded = loadCurrentPhrasePicture();
        if (!currentPhraseLoaded) {

            // preclude a chromatic scale pattern from being the first kind of
            // pattern the player hears:
            Scale scale = getRandomScale();
            if (scaleSets.size() > 1) {
                while (scale.getClassName().equals("Chromatic")) {
                    scale = getRandomScale();
                }
            }
            currentPhrase = generateShortReichLikePhrase(scale);
            currentPhrasePicture = new PhrasePicture(currentPhrase, "a", this);
            addPhrasePicture(currentPhrasePicture);
            saveCurrentPhrasePicture();
        }
    }

    /**
     * Gives the lowermost y-coordinate of the top-toolbar.
     * 
     * @return The lowermost y-coordinate of the top-toolbar.
     */
    public static int topToolbarY2() {
        return 70;
    }

    /**
     * Initializes the buttons that change the screen.
     */
    private void initChangeScreenButtons(Screen currentScreen) {
        int x1 = 10;
        int y1 = 2;
        int width = PRESENTER_BUTTON_X2 - 10;
        int height = 16;

        presentButton = consChangeScreenButton("toPresenter", "Play", x1, y1, width, height);
        y1 += height + 5;
        composeButton = consChangeScreenButton("toEditor", "Compose", x1, y1, width, height);
        y1 += height + 5;
        loadButton = consChangeScreenButton("toPhraseRepo", "Load", x1, y1, width, height);

        helpButton = consChangeScreenButton("toHelp", "Help", (int) Util.getX2(presentButton) + CONTROLLER_DX,
                (int) Util.getY1(presentButton) + height / 2 + 2, 90, height);

        updateHighlightedChangeScreenButton(currentScreen);
    }

    /**
     * Constructs a change screen button.
     * 
     * @param callbackName
     *            The name of the callback that is called when the change screen
     *            button produces an event.
     * @param labelText
     *            The name that appears on the button.
     * @param x1
     *            The leftmost x-coordinate of the button.
     * @param y1
     *            The uppermost y-coordinate of the button.
     * @param width
     *            The width of the button.
     * @param height
     *            The height of the button.
     * @return The button.
     */
    private Button consChangeScreenButton(String callbackName, String labelText, int x1, int y1, int width,
            int height) {
        Button b = cp5.addButton(callbackName).setPosition(x1, y1).setSize(width, height).setLabel(labelText);
        b.getCaptionLabel().toUpperCase(false);
        b.getCaptionLabel().setFont(pfont14);
        colorControllerShowingLabel(b);
        return b;
    }

    /**
     * Updates which change-screen button is highlighted based on the current
     * screen.
     * 
     * @param currentScreen
     *            The current screen.
     */
    private void updateHighlightedChangeScreenButton(Screen currentScreen) {
        if (currentScreen == presenter) {
            highlight(presentButton);
        } else if (currentScreen == editor) {
            highlight(composeButton);
        } else if (currentScreen == phraseRepo) {
            highlight(loadButton);
        } else if (currentScreen == help) {
            highlight(helpButton);
        }
    }

    /**
     * Updates which change-screen buttons are highlighted based on the current
     * screen.
     * 
     * @param currentScreen
     *            The current screen.
     */
    private void updateHiddenChangeScreenButtons(Screen currentScreen) {
        if (currentScreen == editor || currentScreen == help) {
            helpButton.show();
        } else {
            helpButton.hide();
        }
    }

    /**
     * Highlights the given change screen button, and un-highlights all other
     * change screen buttons.
     * 
     * @param changeScreenButton
     *            The change screen button to highlight.
     */
    private void highlight(Button changeScreenButton) {
        changeScreenButton.setColorBackground(getColor1Bold());

        if (changeScreenButton != presentButton) {
            presentButton.setColorBackground(getColor1());
        }
        if (changeScreenButton != composeButton) {
            composeButton.setColorBackground(getColor1());
        }
        if (changeScreenButton != loadButton) {
            loadButton.setColorBackground(getColor1());
        }
        if (changeScreenButton != helpButton) {
            helpButton.setColorBackground(getColor1());
        }
    }

    /**
     * Initializes a red / blue color scheme.
     */
    private void initColorScheme() {
        colorMode(HSB, 360, 100, 100, 100);
        int color1 = color(0, 60, 90);
        int color1Bold = color(0, 90, 90);
        int color1VeryBold = color(0, 100, 100);
        int color2 = color(240, 60, 90);
        int color2Bold = color(240, 90, 90);
        int color2VeryBold = color(240, 100, 100);
        colorMode(RGB, 255, 255, 255, 255);
        colorScheme = new ColorScheme(color1, color2, color1Bold, color2Bold, color1VeryBold, color2VeryBold);
    }

    /**
     * Initializes a color scheme composed of a random color and a gray.
     */
    private void initSimpleColorScheme() {
        int r = (int) random(255);
        int g = (int) random(255);
        int b = (int) random(255);

        int color1 = color(r, g, b, 150);
        int color1Bold = color(r, g, b, 255);
        int color2 = color(100);
        int color2Bold = color(0);

        colorScheme = new ColorScheme(color1, color2, color1Bold, color2Bold, color1Bold, color2Bold);
    }

    /****************************
     ***** Saving / Loading *****
     ****************************/

    /**
     * Looks at all the JSON files in the data/scales folder and tries to
     * translate them into scale sets, which go into the PhasesPApplet member
     * variable "scaleSets".
     */
    private void loadScales() {
        try {
            int i = 0;
            Files.walk(Paths.get(dataFolderPath + "scales")).forEach(filePath -> {
                if (filePath.toString().endsWith(".json")) {
                    BufferedReader reader = createReader(filePath.toString());
                    JSONObject json = new JSONObject(reader);
                    ScaleSet ss = new ScaleSet(json);
                    scaleSets.put(ss.getName(), ss);
                    scaleTypes.add(ss.getName());
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to initialize the current phrase and current phrase picture by
     * loading the appropriate file.
     * 
     * @return True, if it succeeds. False if it fails.
     */
    private boolean loadCurrentPhrasePicture() {
        File file = new File(saveFolderPath + "phrases/Current Phrase.json");
        if (file.exists()) {
            try {
                BufferedReader reader = createReader(file);
                JSONObject json = new JSONObject(reader);
                currentPhrasePicture = new PhrasePicture(json);
                currentPhrase = currentPhrasePicture.getPhrase();
                reader.close();
                return true;
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Saves the phrase picture of the current phrase to the phrases subfolder
     * in the save folder.
     */
    public void saveCurrentPhrasePicture() {
        saveJSONObject(currentPhrasePicture.toJSON(), saveFolderPath + "phrases/Current Phrase.json");
    }

    /**
     * Tries to initialize the playerInfo variable by loading the appropriate
     * file.
     * 
     * @return True, if it succeeds. False if it fails.
     */
    private boolean loadPlayerInfo() {
        String playerInfoFileName = saveFolderPath + "playerInfo.json";
        File playerInfoFile = new File(playerInfoFileName);
        if (playerInfoFile.exists()) {
            try {
                BufferedReader reader = createReader(playerInfoFile);
                JSONObject json = new JSONObject(reader);
                playerInfo = new PlayerInfo(json);
                reader.close();
                return true;
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Saves the player info to a file in the save folder.
     */
    public void savePlayerInfo() {
        JSONObject json = playerInfo.toJSON();
        try {
            saveJSONObject(json, saveFolderPath + "playerInfo.json");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the given PhrasePicture to the phrases subfolder of the save
     * folder.
     * 
     * @param p
     *            The PhrasePicture.
     * @return True if the operation is successful, false otherwise.
     */
    private boolean savePhrasePicture(PhrasePicture p) {
        try {
            saveJSONObject(p.toJSON(), saveFolderPath + "phrases/" + p.getName() + ".json");
            return true;
        } catch (RuntimeException e) {
            System.out.println("runtime exception encountered saving phrase picture.");
            return false;
        }
    }

    /**
     * Saves all the PhrasePictures contained in the phrasePictures list.
     */
    private void savePhrasePictures() {
        for (PhrasePicture p : phrasePictures) {
            savePhrasePicture(p);
        }
    }

    /**
     * Loads the phrase pictures from the phrases subfolder in the save folder,
     * except for any file named "Current Phrase".
     * 
     * @return True, if it succeeds. False, if it fails.
     */
    public boolean loadPhrasePictures() {
        try {
            phrasePictures = new ArrayList<PhrasePicture>();
            Path path = Paths.get(saveFolderPath + "phrases/");
            Files.walk(path).forEach(filePath -> {
                if (filePath.toString().endsWith(".json") && !filePath.toString().contains("Current Phrase.json")) {

                    BufferedReader reader = createReader(filePath.toString());
                    JSONObject json = new JSONObject(reader);
                    phrasePictures.add(new PhrasePicture(json));
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Tries to delete the PhrasePicture located in the phrases subfolder of the
     * save folder that has the given name and should succeed in doing so if the
     * file exists.
     * 
     * @param name
     *            The name (w/o the extension) of the file.
     */
    private void deletePhrasePictureFile(String name) {
        File file = new File(saveFolderPath + "phrases/" + name + ".json");
        if (file.exists()) {
            try {
                boolean success = file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("file does not exist.");
        }
    }

    /*****************************
     ***** Phrase Generation *****
     *****************************/

    /**
     * 
     * @return The score from Steve Reich's Piano Phase (1967)
     */
    public Phrase pianoPhase() {
        int n = Phrase.NOTE_START;
        return currentPhrase = new Phrase(new float[] { 64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73 },
                repeat(defaultDynamic(), 12), new int[] { n, n, n, n, n, n, n, n, n, n, n, n }, "Chromatic", "E");
    }

    /**
     * 
     * @return A random scale contained by this PhasesPApplet object.
     */
    public Scale getRandomScale() {
        String type = scaleTypes.get((int) random(scaleTypes.size()));
        ScaleSet s = scaleSets.get(type);
        return s.getScale((int) random(s.numScales()));
    }

    /**
     * Generates a phrase that has the property of being like the phrase in
     * Steve Reich's Piano Phase in this sense: every pitch in the phrase occurs
     * periodically.
     * 
     * A phasing process over a phrase like this as opposed to a generic phrase
     * results in a greater frequence of unisons (two notes of the same pitch
     * playing at the same time).
     * 
     * @return The generated phrase.
     */
    public Phrase generateReichLikePhrase() {
        return generateReichLikePhrase(getRandomScale());
    }

    /**
     * Generates a phrase that has the property of being like the phrase in
     * Steve Reich's Piano Phase in this sense: every pitch in the phrase occurs
     * periodically.
     * 
     * A phasing process over a phrase like this as opposed to a generic phrase
     * results in a greater frequence of unisons (two notes of the same pitch
     * playing at the same time).
     * 
     * @param scale
     *            The set of pitches the phrase draws from.
     * 
     * @return The generated phrase.
     */
    public Phrase generateReichLikePhrase(final Scale scale) {
        float r = random(1);
        int startingOctave = (r < 0.7f) ? MIN_OCTAVE + 1 : MIN_OCTAVE;
        return generateReichLikePhrase(scale, startingOctave, (random(1) < 0.5f));
    }

    /**
     * Generates a Reich-like Phrase that is guarenteed to be short (not contain
     * many notes).
     * 
     * Generates a phrase that has the property of being like the phrase in
     * Steve Reich's Piano Phase in this sense: every pitch in the phrase occurs
     * periodically.
     * 
     * A phasing process over a phrase like this as opposed to a generic phrase
     * results in a greater frequence of unisons (two notes of the same pitch
     * playing at the same time).
     * 
     * @param scale
     *            The set of pitches the phrase draws from.
     * 
     * @return The generated phrase.
     */
    public Phrase generateShortReichLikePhrase(final Scale scale) {
        return generatePhraseFromTemplates(new String[] { "ABCDAECF", "ABCDABCE" }, scale, 5, random(1) < 0.5);
    }

    /**
     * Generates a phrase that has the property of being like the phrase in
     * Steve Reich's Piano Phase in this sense: every pitch in the phrase occurs
     * periodically.
     * 
     * A phasing process over a phrase like this as opposed to a generic phrase
     * results in a greater frequence of unisons (two notes of the same pitch
     * playing at the same time).
     * 
     * @param scale
     *            The set of pitches the phrase draws from.
     * @param octave
     *            The starting octave. Pitches in the phrase may be above this
     *            octave, but not below it.
     * @param allowRests
     *            A boolean that indicates whether or not a rest can stand in
     *            for a pitch.
     * 
     * 
     * @return The generated phrase.
     */
    public Phrase generateReichLikePhrase(final Scale scale, final int octave, final boolean allowRests) {
        return generatePhraseFromTemplates(new String[] { "ABCDAECF", "ABCDABCE", "ABCDEBADCBED", "ABCDEBADCBED",
                "ABCDEBADFBEDABGDEBADHBED", "ABCDAECFADCEAGCDAECHADCE" }, scale, octave, allowRests);
    }

    /**
     * Generates a phrase from one of the given templates.
     * 
     * A template is a string of characters. Each character is like a variable.
     * It represents some pitch. This function randomly binds every character in
     * the template to a pitch in the scale.
     * 
     * @param templates
     *            The set of templates.
     * @param scale
     *            The set of pitches the phrase draws from.
     * @param minOctave
     *            The starting octave. Pitches in the phrase may be above this
     *            octave, but not below it.
     * @param allowRests
     *            A boolean that indicates whether or not a rest can stand in
     *            for a pitch.
     * @return The generated phrase.
     */
    public Phrase generatePhraseFromTemplates(final String[] templates, final Scale scale, final int minOctave,
            final boolean allowRests) {
        // choose template
        final int REST = -1;
        String template = templates[(int) random(templates.length)];
        HashMap<Character, Integer> map = new HashMap<Character, Integer>();

        // init set of pitch choices
        int numPitchesNeeded = numUniqueChars(template);
        int numOctavesNeeded = ceil((float) numPitchesNeeded / (float) scale.size());
        int numPitchChoices = numOctavesNeeded * scale.size();

        if (allowRests)
            numPitchChoices++;
        int[] pitchChoices = new int[numPitchChoices];
        for (int i = 0; i < pitchChoices.length; i++) {
            pitchChoices[i] = scale.getNoteValue(i) + minOctave * 12;
        }
        if (allowRests)
            pitchChoices[pitchChoices.length - 1] = REST;

        shuffle(pitchChoices);

        // init phrase components
        float[] pitches = new float[template.length()];
        float[] dynamics = new float[template.length()];
        int[] cellTypes = new int[template.length()];

        int j = 0; // loops through pitch choices
        for (int i = 0; i < template.length(); i++) { // loops through template
            char c = template.charAt(i);
            if (!map.containsKey(c)) {
                int pitch = pitchChoices[j];
                j++;
                map.put(c, pitch);
            }
            int pitch = map.get(c);
            pitches[i] = pitch;
            dynamics[i] = (pitch == REST) ? 0 : defaultDynamic();
            cellTypes[i] = (pitch == REST) ? Phrase.REST : Phrase.NOTE_START;
        }

        Phrase phrase = new Phrase(pitches, dynamics, cellTypes, scale.getClassName(), scale.getName());

        return phrase;
    }

    /**
     * Counts the number of unique characters in the given string and returns
     * the result.
     * 
     * @param s
     *            The string.
     * @return The number of unique characters in the given string.
     */
    private int numUniqueChars(String s) {
        String uniqueChars = "";

        for (int i = 0; i < s.length(); i++) {
            if (!uniqueChars.contains("" + s.charAt(i))) {
                uniqueChars += s.charAt(i);
            }
        }

        return uniqueChars.length();
    }

    /**
     * Randomly shuffles an array of ints.
     * 
     * @param xs
     *            The array of ints to shuffle.
     */
    private void shuffle(int[] xs) {
        int i = xs.length - 1;
        while (i > 0) {
            int j = (int) random(0, i + 1);
            swap(xs, i, j);
            i--;
        }
    }

    /**
     * Swaps two ints within an int array.
     * 
     * @param xs
     *            The int array.
     * @param i
     *            The index of one int.
     * @param j
     *            The index of the other int.
     */
    private static void swap(int[] xs, int i, int j) {
        int xs_i = xs[i];
        xs[i] = xs[j];
        xs[j] = xs_i;
    }

    /*****************************
     ***** ControlP5 Related *****
     *****************************/

    /**
     * The PhasesPApplet does not draw its own controllers, because sometimes a
     * more customized draw order is required.
     * 
     * This method draws and updates the PhasesPApplet's controllers.
     */
    public void drawControlP5() {
        cp5.draw();
    }

    /**
     * Makes all the PhasesPApplet's controllers invisible.
     */
    public void hideAllControllers() {
        hideChangeScreenButtons();
    }

    /**
     * Makes all the PhasesPApplet's controllers related to changing the screen
     * invisible.
     */
    public void hideChangeScreenButtons() {
        presentButton.hide();
        composeButton.hide();
        loadButton.hide();
    }

    /**
     * Makes all the PhasesPApplet's controllers visible.
     */
    public void showAllControllers() {
        showChangeScreenButtons();
    }

    /**
     * Makes all the PhasesPApplet's controllers related to changing the screen
     * visible.
     */
    public void showChangeScreenButtons() {
        presentButton.show();
        composeButton.show();
        loadButton.show();
    }

    /**
     * Gives the default coloring for labeled buttons to the given controller.
     * 
     * @param c
     *            The given controller.
     */
    public static void colorControllerShowingLabel(Controller c) {
        c.setColorCaptionLabel(0xffffffff);
        c.setColorValueLabel(0xffffffff);
        c.setColorBackground(getColor1());
        c.setColorForeground(getColor1Bold());
        c.setColorActive(getColor1Bold());
    }

    /**
     * Gives the default coloring for unlabeled buttons to the given button.
     * 
     * @param b
     *            The given button.
     */
    public void colorControllerHideLabel(Button b) {
        b.setColorBackground(color(255));
        b.setColorForeground(getColor1());
        b.setColorActive(getColor1Bold());
    }

    /***********************************
     ***** Callback from ControlP5 *****
     ***********************************/

    /**
     * Callback from ControlP5. Changes the current screen to the Presenter
     * screen.
     */
    public void toPresenter() {
        if (currentScreen != presenter) {
            changeScreenTo(presenter);
        }
    }

    /**
     * Callback from ControlP5. Changes the current screen to the Help screen.
     */
    public void toHelp() {
        if (currentScreen != help) {
            changeScreenTo(help);
        }
    }

    /**
     * Callback from ControlP5. Changes the current screen to the Editor screen.
     */
    public void toEditor() {
        if (currentScreen != editor) {
            changeScreenTo(editor);
        }
    }

    /**
     * Callback from ControlP5. Changes the current screen to the PhraseRepo
     * screen.
     */
    public void toPhraseRepo() {
        if (currentScreen != phraseRepo) {
            changeScreenTo(phraseRepo);
        }
    }

    /**
     * Changes the current screen from what it currently is to the given screen.
     * 
     * @param destination
     *            The screen to change to.
     */
    private void changeScreenTo(Screen destination) {
        if (currentScreen != null) {
            currentScreen.onExit();
        }
        prevScreen = currentScreen;
        currentScreen = destination;
        currentScreen.onEnter();
        updateHighlightedChangeScreenButton(currentScreen);
        updateHiddenChangeScreenButtons(currentScreen);
    }

    /**
     * Gives what the caption label should be for the given screen.
     * 
     * @param screen
     *            The screen.
     * @return The caption label.
     */
    private String captionLabel(Screen screen) {
        if (screen == presenter) {
            return "Compose";
        } else if (screen == editor) {
            return "Rehearse";
        } else if (screen == phraseRepo) {
            return "Back";
        } else {
            return "";
        }

    }

    /*********************
     ***** Draw Loop *****
     *********************/

    /**
     * Sends a message to the current screen to draw itself.
     */
    public void draw() {
        checkForWindowResizeEvent();
        currentScreen.draw();
    }

    /********************************
     ***** Input Event Handling *****
     ********************************/

    /**
     * Sends mouse pressed events to the current screen.
     */
    public void mousePressed() {
        currentScreen.mousePressed();
    }

    /**
     * Sends mouse released events to the current screen.
     */
    public void mouseReleased() {
        currentScreen.mouseReleased();
    }

    /**
     * Sends mouse dragged events to the current screen.
     */
    public void mouseDragged() {
        currentScreen.mouseDragged();
    }

    /**
     * Sends mouse moved events to the current screen.
     */
    public void mouseMoved() {
        currentScreen.mouseMoved();
    }

    /**
     * Handles pause / unpause requests. Sends key pressed events to the current
     * screen.
     */
    public void keyPressed() {
        if (key == ESC) {
            key = 0; // disables Processing's default behavior to close the
                     // program when ESC is pressed
        } else {
            currentScreen.keyPressed();
        }
    }

    /**
     * Sends key released events to the current screen.
     */
    public void keyReleased() {
        currentScreen.keyReleased();

    }

    /**
     * Sends mouse wheel events to the current screen.
     */
    public void mouseWheel(MouseEvent event) {
        currentScreen.mouseWheel(event);
    }

    /************************
     ***** Other Events *****
     ************************/

    @Override
    public void exitActual() {
        presenter.saveViewSettings();
        this.savePlayerInfo();
        this.saveCurrentPhrasePicture();
        this.savePhrasePictures();
        super.exitActual();
    }

    /**
     * The way to resize the PhasesPApplet through code logic.
     * 
     * @param width
     *            The new width for the window.
     * @param height
     *            The new height for the window.
     */
    public void resize(int width, int height) {
        surface.setSize(width, height);
        checkForWindowResizeEvent();
    }

    /**
     * Checks if the window size has changed. If so, informs the currentScreen
     * of the size change.
     * 
     */
    private void checkForWindowResizeEvent() {
        if (prevWidth != width || prevHeight != height) {
            prevWidth = width;
            prevHeight = height;
            currentScreen.windowResized();
        }
    }

    /************************************************
     ***** Extended Primitive Drawing Functions *****
     ************************************************/

    /**
     * Draws a plus (+) centered about (cenx, ceny) and with the given radius.
     * 
     * @param cenx
     *            The center x-coordinate.
     * @param ceny
     *            The center y-coordinate.
     * @param radius
     *            The radius.
     */
    public void drawPlus(float cenx, float ceny, float radius) {
        line(cenx - radius, ceny, cenx + radius, ceny);
        line(cenx, ceny - radius, cenx, ceny + radius);
    }

    /**
     * Draws an arrow head from two lines (which looks like this: >). It has its
     * tip at (x,y). The lines have a given length. The headAngle tells the
     * arrow head which direction to point, in radians The deviationAngle tells
     * the lines how much to bend away from the head angle, in radians.
     * 
     * @param x
     *            The x-coordinate of the arrow head's tip
     * @param y
     *            The y-coordinate of the aroow head's tip
     * @param length
     *            The length of each of the lines
     * @param headAngle
     *            The direction in which the arrow head points, in radians
     * @param deviationAngle
     *            How much the lines bend away from the head angle, in radians
     * @param pg
     *            The PGraphics instance on which to draw the arrow head
     */
    public static void drawArrowHead(float x, float y, float leng, float headAngle, float deviationAngle,
            PGraphics pg) {
        pg.noFill();
        pg.beginShape();
        pg.vertex(x + leng * cos(headAngle + deviationAngle), y + leng * sin(headAngle + deviationAngle));
        pg.vertex(x, y);
        pg.vertex(x + leng * cos(headAngle - deviationAngle), y + leng * sin(headAngle - deviationAngle));
        pg.endShape();
    }

    /**
     * Draws an arrow head from two lines (which looks like this: >). It has its
     * tip at (x,y). The lines have a given length. The headAngle tells the
     * arrow head which direction to point, in radians The deviationAngle tells
     * the lines how much to bend away from the head angle, in radians.
     * 
     * @param x
     *            The x-coordinate of the arrow head's tip
     * @param y
     *            The y-coordinate of the aroow head's tip
     * @param leng
     *            The length of each of the lines
     * @param headAngle
     *            The direction in which the arrow head points, in radians
     * @param deviationAngle
     *            How much the lines bend away from the head angle, in radians
     */
    public void drawArrowHead(float x, float y, float leng, float headAngle, float deviationAngle) {
        noFill();
        beginShape();
        vertex(x + leng * cos(headAngle + deviationAngle), y + leng * sin(headAngle + deviationAngle));
        vertex(x, y);
        vertex(x + leng * cos(headAngle - deviationAngle), y + leng * sin(headAngle - deviationAngle));
        endShape();
    }

    /**
     * Draws a quarter note symbol, as in standard musical notation, centered
     * about (cenx, ceny).
     * 
     * @param cenx
     *            The center x-coordinate
     * @param ceny
     *            The center y-coordinate
     * @param textSize
     *            The size of the font that draws the symbol
     */
    public void drawQuarterNoteSymbol(float cenx, float ceny, int textSize) {
        pushStyle();
        textFont(musicFont);
        textSize(textSize);
        textAlign(CENTER, CENTER);
        text("q", cenx, ceny - textSize * 0.2f);
        popStyle();
    }

    /**
     * Draws a camera icon with upperleft corner (x1, y1), width w, and height
     * h.
     * 
     * @param x1
     *            The leftmost x-coordinate
     * @param y1
     *            The uppermost y-coordinate
     * @param w
     *            The width
     * @param h
     *            The height
     */
    public void drawCameraIcon(float x1, float y1, float w, float h) {
        noStroke();
        fill(0);
        rectMode(CORNER);
        rect(x1 - 0.3f * w, y1 - 0.5f * h, 0.8f * w - 5, h);
        noStroke();
        fill(0);
        Polygon.drawPolygon(x1 - 0.3f * w - 0.1f * w, y1, 0.2f * w, 0.2f * w, 3, 0, this);
    }

    /**
     * Draws a transverse sine wave centered about (cenx, ceny) with a given
     * length and amplitude.
     * 
     * @param cenx
     *            The center x-coordinate
     * @param ceny
     *            The center y-coordinate
     * @param length
     *            The length
     * @param amp
     *            The amplitude
     */
    public void drawSineWave(float cenx, float ceny, float length, float amp) {
        drawSineWave(cenx, ceny, length, amp, 0);
    }

    /**
     * Draws a transverse sine wave centered about (cenx, ceny) with a given
     * length, amplitude, and a starting angle, in radians, which displaces the
     * wave.
     * 
     * @param cenx
     *            The center x-coordinate
     * @param ceny
     *            The center y-coordinate
     * @param length
     *            The length
     * @param amp
     *            The amplitude
     * @param startAngle
     *            The starting angle, in radians
     */
    public void drawSineWave(float cenx, float ceny, float length, float amp, float startAngle) {
        int numPts = 100;
        float dTheta = TWO_PI / numPts;
        float theta = startAngle;
        float radius = length / 2f;
        float x = cenx - radius;
        float dx = length / numPts;

        for (int i = 0; i < numPts; i++) {
            point(x, ceny + amp * sin(theta));
            x += dx;
            theta += dTheta;
        }
    }

    /*****************************
     ***** Utility Functions *****
     *****************************/

    /**
     * Saves the pixels on the canvas contained within the given rectangle to an
     * image file.
     * 
     * @param rect
     *            The rectangle.
     * @param filename
     *            The name to give the saved image file.
     */
    public void saveImage(Rect rect, String filename) {
        int rectWidth = ceil(rect.getX2()) - floor(rect.getX1());
        int rectHeight = ceil(rect.getY2()) - floor(rect.getY1());
        PImage img = createImage(rectWidth, rectHeight, ARGB);

        img.loadPixels();
        this.loadPixels();

        int i = 0;
        int y = (int) rect.getY1();
        while (y < rect.getY2()) {
            int x = (int) rect.getX1();
            while (x < rect.getX2()) {
                img.pixels[i] = this.pixels[y * width + x];
                i++;
                x++;
            }
            y++;
        }

        img.updatePixels();
        this.updatePixels();

        img.save(filename);
    }

    // Algorithm is somewhat sound, but score.writeMidiFile() won't produce a
    // playable MIDI file.
    public void phraseToMidiFile(Phrase phrase, String location, String name) {
        SCScore score = new SCScore();

        final float ERROR_TOLERANCE = 0.01f;
        final int NOTE = 0, REST = 1;
        float multiplier = PApplet.max(bpm1, bpm2) / PApplet.min(bpm1, bpm2);
        float notept1 = 0, notept2 = 0;
        do {

            // add all the notes from player 1
            for (int i = 0; i < phrase.getNumNotes(); i++) {
                float dur = phrase.getSCDuration(i);
                score.addNote(notept1, 0, 0, phrase.getSCPitch(i), phrase.getSCDynamic(i), dur,
                        phrase.getSCArticulation(i), phrase.getSCPan(i));
                notept1 += dur;
            }

            // add all the notes from player 2
            if (notept2 < notept1) {
                for (int i = 0; i < phrase.getNumNotes(); i++) {
                    float dur = phrase.getSCDuration(i) * multiplier;
                    score.addNote(notept2, 0, 0, phrase.getSCPitch(i), phrase.getSCDynamic(i), dur,
                            phrase.getSCArticulation(i), phrase.getSCPan(i));
                    notept2 += dur;
                }
            }
        } while (abs(notept1 - notept2) > ERROR_TOLERANCE);

        score.tempo(PApplet.min(bpm1, bpm2));
        score.writeMidiFile(location + "/" + name + ".mid");
    }

    /**
     * A generalization of the other remainder function. In normal modulo
     * arithmetic, the upper number is constrained. This is a generalization of
     * that where the lower number can also be constrained.
     * 
     * @param num
     * @param min
     * @param max
     * @return
     */
    public static int remainder(int num, int min, int max) {
        return remainder(num - min, max - min) + min;
    }

    /**
     * Calculates the remainder of num / denom.
     * 
     * @param num
     *            The numerator
     * @param denom
     *            The denominator
     * @return The remainder of num / denom
     */
    public static int remainder(int num, int denom) {
        if (0 <= num && num < denom)
            return num;
        else if (num > 0)
            return num % denom;
        else
            return (denom - ((-num) % denom)) % denom;
    }

    /**
     * Calculates the remainder of num / denom.
     * 
     * @param num
     *            The numerator
     * @param denom
     *            The denominator
     * @return The remainder of num / denom
     */
    public static float remainder(float num, float denom) {
        if (0 <= num && num < denom)
            return num;
        else if (num > 0)
            return num % denom;
        else
            return denom - ((-num) % denom);
    }

    /**
     * Takes an amount in milliseconds and converts that amount to minutes.
     * 
     * @param amt
     *            The amount in milliseconds.
     * @return The amount in minutes.
     */
    public static float millisToMinutes(int amt) {
        return amt * minutesPerMillisecond;
    }

    /**
     * Takes an x and an n and returns an array of n xs.
     * 
     * @param x
     * @param n
     * @return An int array of n xs.
     */
    public static int[] repeat(int x, int n) {
        int[] xs = new int[n];
        for (int i = 0; i < n; i++) {
            xs[i] = x;
        }
        return xs;
    }

    /**
     * Takes an x and an n and returns an array of n xs.
     * 
     * @param x
     * @param n
     * @return A float array of n xs.
     */
    public static float[] repeat(float x, int n) {
        float[] xs = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = x;
        }
        return xs;
    }

    /*******************************
     ***** Getters and Setters *****
     ******************************/

    /**
     * Returns the scale matching the given root name and scale name, or null if
     * no scale matches.
     * 
     * @param root
     *            The name of the root (e.g. "A", "Bb", ...).
     * @param scaleName
     *            The name of the scale (e.g. "Major", "Minor Pentatonic", ...).
     * @return The matching scale, or null if no scale matches.
     */
    public Scale getScale(String root, String scaleName) {
        for (String name : scaleTypes) {
            if (name.equals(scaleName)) {
                ScaleSet ss = scaleSets.get(name);
                for (int i = 0; i < ss.numScales(); i++) {
                    String scaleRootName = ss.getScale(i).getNoteNameByIndex(0);

                    if (noteNamesAreEquivalent(root, scaleRootName)) {
                        return ss.getScale(i);
                    }
                }
                break;
            }
        }
        return null;
    }

    /**
     * Takes two strings, which presumably represent note names, and compares
     * them, returning true if the two strings represent the same note.
     * 
     * There are 4 cases in which the two names are taken to be equal. (1)
     * name1.equals(name2) (2) name1.contains("/") && name1.contains(name2) &&
     * name2.length() == 2 (3) name2.contains("/") && name2.contains(name1) &&
     * name1.length() == 2 (4) The two strings reference the same pitch, but by
     * different aliases.
     * 
     * Cases (2) and (3) are present so to the method returns true on instances
     * where a name gives two aliases for a note, like:
     * noteNamesAreEquivalent("A#/Bb", "A#").
     * 
     * Example of case (4): name1.equals("A#") && name2.equals("Bb").
     * 
     * @param name1
     *            The first note name.
     * @param name2
     *            The second note name.
     * @return True, if the two strings are taken to refer to the same note,
     *         false otherwise.
     */
    private static boolean noteNamesAreEquivalent(String name1, String name2) {
        if (name1.equals(name2)) {
            return true;
        } else if (name2.length() == 2 && name1.contains("/") && name1.contains(name2)) {
            return true;
        } else if (name1.length() == 2 && name2.contains("/") && name2.contains(name1)) {
            return true;
        } else {
            switch (name1) {
            case "A#":
                if (name2.equals("Bb"))
                    return true;
                break;
            case "Bb":
                if (name2.equals("A#"))
                    return true;
                break;
            case "C#":
                if (name2.equals("Db"))
                    return true;
                break;
            case "Db":
                if (name2.equals("C#"))
                    return true;
                break;
            case "D#":
                if (name2.equals("Eb"))
                    return true;
                break;
            case "Eb":
                if (name2.equals("D#"))
                    return true;
                break;
            case "F#":
                if (name2.equals("Gb"))
                    return true;
                break;
            case "Gb":
                if (name2.equals("F#"))
                    return true;
                break;
            case "G#":
                if (name2.equals("Ab"))
                    return true;
                break;
            case "Ab":
                if (name2.equals("G#"))
                    return true;
                break;
            }
            return false;
        }
    }

    /**
     * 
     * @return color 1 of the program-wide color scheme
     */
    public static int getColor1() {
        return colorScheme.color1;
    }

    /**
     * 
     * @return The bold version of color 1 of the program-wide color scheme
     */
    public static int getColor1Bold() {
        return colorScheme.color1Bold;
    }

    /**
     * 
     * @return The very bold version of color 1 of the program-wide color scheme
     */
    public static int getColor1VeryBold() {
        return colorScheme.color1VeryBold;
    }

    /**
     * 
     * @return color 2 of the program-wide color scheme
     */
    public static int getColor2() {
        return colorScheme.color2;
    }

    /**
     * 
     * @return The bold version of color 2 of the program-wide color scheme
     */
    public static int getColor2Bold() {
        return colorScheme.color2Bold;
    }

    /**
     * 
     * @return The very bold version of color 2 of the program-wide color scheme
     */
    public static int getColor2VeryBold() {
        return colorScheme.color2VeryBold;
    }

    /**
     * 
     * @return The color halfway in between color 1 and color 2 of the
     *         program-wide color scheme
     */
    public static int getBlendedColor() {
        return colorScheme.blendedColor;
    }

    /**
     * 
     * @return The color halfway in between color of color 1 bold and color 2
     *         bold of the program-wide color scheme
     */
    public static int getBlendedColorBold() {
        return colorScheme.blendedColorBold;
    }

    /**
     * 
     * @param amt
     *            The lerp amt, a value between 0 and 1.
     * @return A color in between color 1 and color 2 of the program-wide color
     *         scheme
     */
    public static int getBlendedColor(float amt) {
        return lerpColor(colorScheme.color1, colorScheme.color2, amt, PApplet.RGB);
    }

    /**
     * 
     * @param amt
     *            The lerp amt, a value between 0 and 1.
     * @return A color in between color 1 and color 2 of the program-wide color
     *         scheme
     */
    public static int getBlendedColorBold(float amt) {
        return PApplet.lerpColor(colorScheme.color1Bold, colorScheme.color2Bold, amt, PApplet.RGB);
    }

    /**
     * 
     * @return The beats per minute of the first piano player
     */
    public float getBPM1() {
        return bpm1;
    }

    /**
     * 
     * @return The beats per millisecond of the first piano player
     */
    public float getBPMS1() {
        return bpms1;
    }

    /**
     * 
     * @return The beats per minute of the second piano player
     */
    public float getBPM2() {
        return bpm2;
    }

    /**
     * 
     * @return The beats per millisecond of the second piano player
     */
    public float getBPMS2() {
        return bpms2;
    }

    /**
     * Sets the beats per minute of the first piano player
     * 
     * @param bpm1
     */
    public void setBPM1(float bpm1) {
        this.bpm1 = bpm1;
        this.bpms1 = bpm1 / 60000f;
    }

    /**
     * Sets the beats per minute of the second piano player
     * 
     * @param bpm2
     */
    public void setBPM2(float bpm2) {
        this.bpm2 = bpm2;
        this.bpms2 = bpm2 / 60000f;
    }

    /**
     * Gives the rightmost x-coordinate of the help button.
     * 
     * @return The rightmost x-coordinate of the help button.
     */
    public static float getHelpButtonX2() {
        return HELP_BUTTON_X2;
    }

    /**
     * Gives the rightmost x-coordinate of the present button.
     * 
     * @return The rightmost x-coordinate of the present button.
     */
    public static float getPresentButtonX2() {
        return PRESENTER_BUTTON_X2;
    }

    /**
     * Sets the current scale according to the given scale name.
     * 
     * @param scaleClassName
     * @param scaleRootName
     * @return True, if the current scale was successfully assigned a value,
     *         false if no value was assigned to it.
     */
    public boolean setCurrentScale(String scaleClassName, String scaleRootName) {
        Scale newScale = getScale(scaleRootName, scaleClassName);
        if (newScale != null) {
            this.currentScale = newScale;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gives the number of PhrasePictures contained by this PhasesPApplet.
     * 
     * @return The number of PhrasePictures
     */
    public int getNumPhrasePictures() {
        return phrasePictures.size();
    }

    /**
     * Gives the PhrasePicture at the given index.
     * 
     * @param i
     *            The index.
     * @return The PhrasePicture.
     */
    public PhrasePicture getPhrasePicture(int i) {
        return phrasePictures.get(i);
    }

    /**
     * Adds the given PhrasePicture to the end of this PhasesPApplet's list of
     * PhrasePictures.
     * 
     * @param p
     *            The PhrasePicture.
     */
    public void addPhrasePicture(PhrasePicture p) {
        phrasePictures.add(p);
        savePhrasePicture(p);
    }

    /**
     * Adds the given PhrasePicture at the ith index in this PhasesPApplet's
     * list of PhrasePictures.
     * 
     * @param i
     *            The index.
     * @param p
     *            The PhrasePicture.
     */
    public void addPhrasePicture(int i, PhrasePicture p) {
        phrasePictures.add(i, p);
        savePhrasePicture(p);
    }

    /**
     * Removes the ith index PhrasePicture from the PhasesPApplet's list of
     * PhrasePictures.
     * 
     * @param i
     *            The index.
     */
    public void removePhrasePicture(int i) {
        this.deletePhrasePictureFile(phrasePictures.get(i).getName());
        phrasePictures.remove(i);
    }

    /**
     * Removes the given PhrasePicture from the PhasesPApplet's list of
     * PhrasePictures.
     * 
     * @param p
     *            The PhrasePicture to remove.
     */
    public void removePhrasePicture(PhrasePicture p) {
        try {
            removePhrasePicture(phrasePictures.indexOf(p));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tells whether or not there is another PhrasePicture, other than the given
     * PhrasePicture, that has the given name.
     * 
     * @param phrasePicture
     *            The given PhrasePicture.
     * @param name
     *            The given name.
     * @return True if there is such a PhrasePicture, false otherwise.
     */
    public boolean hasAnotherPhrasePictureWithName(PhrasePicture phrasePicture, String name) {
        for (PhrasePicture p : phrasePictures) {
            if (p.getName().equals(name) && p != phrasePicture) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gives the index of the current phrase picture.
     * 
     * @return The index.
     */
    public int indexOfCurrentPhrasePicture() {
        return phrasePictures.indexOf(currentPhrasePicture);
    }

    /**
     * 
     * @return True, if the mouse is over the PhasesPApplet's present button,
     *         false otherwise.
     */
    public boolean isMouseOverPresentButton() {
        return presentButton.isMouseOver();
    }

    /**
     * 
     * @return True, if the mouse is over the PhasesPApplet's compose button,
     *         false otherwise.
     */
    public boolean isMouseOverComposeButton() {
        return composeButton.isMouseOver();
    }

    /**
     * 
     * @return True, if the mouse is over the PhasesPApplet's load button, false
     *         otherwise.
     */
    public boolean isMouseOverLoadButton() {
        return loadButton.isMouseOver();
    }

    /**
     * 
     * @return True, if the mouse is over the PhasesPApplet's help button, false
     *         otherwise.
     */
    public boolean isMouseOverHelpButton() {
        return helpButton.isMouseOver();
    }

    /**
     * Data container for a color scheme.
     * 
     * @author James Morrow
     *
     */
    private class ColorScheme {
        final int color1, color2, color1Bold, color2Bold, color1VeryBold, color2VeryBold;
        final int blendedColor, blendedColorVeryBold, blendedColorBold;

        private ColorScheme(int color1, int color2, int color1Bold, int color2Bold, int color1VeryBold,
                int color2VeryBold) {
            this.color1 = color1;
            this.color2 = color2;
            this.color1Bold = color1Bold;
            this.color2Bold = color2Bold;
            this.color1VeryBold = color1VeryBold;
            this.color2VeryBold = color2VeryBold;
            blendedColor = lerpColor(color1, color2, 0.5f);
            blendedColorBold = lerpColor(color1Bold, color2Bold, 0.5f);
            blendedColorVeryBold = lerpColor(color1VeryBold, color2VeryBold, 0.5f);
        }
    }

    /**
     * 
     * @return The default dynamic value for notes created in the Editor
     */
    public static float defaultDynamic() {
        return 60 + ((float) Math.random() * 10 - 5);
    }
}