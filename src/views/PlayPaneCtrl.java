package views;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Text;
import javafx.util.Duration;
import utils.WindowApp;

public class PlayPaneCtrl extends WindowApp {

    public static void main(String[] args) { launch(args); }

    @FXML
    private VBox root;

    @FXML
    private VBox vboxVolume;

    @FXML
    private HBox hboxVolume;

    @FXML
    private Label lblVolume;

    @FXML
    private Text txtVolume;

    @FXML
    private Slider sldrVolume;

    public Slider getSldrVolume() {
		return sldrVolume;
	}

	@FXML
    private VBox vboxLecture;

    @FXML
    private HBox hboxLecture;

    @FXML
    private Label lblLecture;

    @FXML
    private Text txtLecture;
    
    @FXML
    private HBox hboxProgres;

    @FXML
    private Button btnBackward;

    @FXML
    private ProgressBar progLecture;    
    
    @FXML
    private Button btnForward;

    @FXML
    private Button btnJouer;

    @FXML
    private Button btnPause;

    @FXML
    private Button btnArret;

    @FXML
    private HBox hboxFichier;

    @FXML
    private Label lblFichier;    
    
	@FXML
    private TextArea areaMetadata;
	
	@FXML
	private ImageView imageViewRight;
	
	@FXML
	private ImageView imageViewLeft;
	 
	private MediaPlayer mediaPlayer = null;
	
	private Consumer<Status> onStatusChange;
	
	private Runnable onEndOfMedia;
	private Media newMedia = null;
	
	private InvalidationListener statusListener = dontcare->{
		onStatusChange.accept(mediaPlayer.getStatus());
	};
	
    private InvalidationListener timeListener = dontcare->updateProgression();
    
    private int lastProgres = -1;
    
    private int lastTotal = -1;

    /**
     * Fait un bind puis démarre la pièce. 
     * @see bindSong
     * 
     * @param newMedia
     * @param songName
     */
    public void bindAndPlay(Media newMedia, String songName)
    {
    	bindSong(newMedia, songName);
    	if(isBinded()) btnJouer.fire();
    }

    
    /**
     * Coupe le player de toute association avec une pièce musicale.
     * C'est l'état par défaut au démarrage du composante.
     */
    public void bindNoSong()
    {
    	unbindMediaPlayer();
    	vboxLecture.setDisable(true);
    	lblFichier.setText("Aucune");
    	areaMetadata.clear();
    	afficherProgressionAuBesoin(0, 0);
    }

    /**
     * Associe le player à une pièce musicale. 
     * Si une pièce est déjà associée, elle sera remplacée.
     * Si une pièce est déjà en train de jouer, la nouvelle pièce démarre automatiquement.
     * 
     * @param newMedia
     * 		La pièce à jouer.
     * 		Si null, on efface la pièce sans la remplacer.
     * 
     * @param songName
     * 		Le nom de la pièce qui doit apparaitre, si la pièce n'a pas de nom.
     * 		Si null, le nom est inconnu. 
     */
    public void bindSong(Media newMedia, String songName)
    {
    	this.newMedia = newMedia;
    	
    	if (newMedia == null) { bindNoSong(); return; }
    	boolean wasPlaying = mediaPlayer != null && mediaPlayer.getStatus() == Status.PLAYING;
    	unbindMediaPlayer();
    	vboxLecture.setDisable(false);
    	if(songName == null) songName = "Inconnu";
    	lblFichier.setText(songName);
    	bindMediaPlayer(newMedia);
    	if(wasPlaying) Platform.runLater(()->btnJouer.fire());
    	else requestFocus(btnJouer);
    }
    
    /**
     * Configure le panneau après initialisation.
     * 
     * @param onStatusChange
     * 		Fonction qui sera appelée chaque fois que le statu du Player va changer.
     * 		Cette fonction recoit en parametre le nouveau statu du player.
     * 		Si null il ne se passera rien à ce moment.
     * 
     * @param onEndOfMedia
     * 		Fonction qui sera appelée chaque fois qu'une pièce prend fin.
     * 		Si null, il ne se passera rien à ce moment.
     */
    public void config(Consumer<Status> onStatusChange, Runnable onEndOfMedia)
    {
    	this.onEndOfMedia = onEndOfMedia != null ? onEndOfMedia : ()->{ };
    	this.onStatusChange = onStatusChange != null ? onStatusChange : status -> {};
    }
    
    
    /**
     * @return La propriété volume  
     */
    public DoubleProperty volumeProperty() { 
    	return sldrVolume.valueProperty(); 
    }
    
    private void afficherMetadonnees()
    {
		List<String> list = new ArrayList<String>();
		mediaPlayer.getMedia().getMetadata().forEach((key,o)->{
			if(!key.startsWith("raw"))
			{
				list.add(key + " : " + o + "\n");
			}				
				
		});
			
		creationTitre(mediaPlayer.getMedia().getMetadata());
		
		list.sort(null);
		areaMetadata.clear();
		if(list.isEmpty()) 
			areaMetadata.appendText("Aucune métadonnées disponibles");
		else
			list.forEach(elem -> areaMetadata.appendText(elem));
		areaMetadata.positionCaret(0);
    }
    
    private void creationTitre(ObservableMap<String, Object> map)
    {
    	String artist = ""; 
    	String year = "";
    	String album = "";
    	String title = "";
    	@SuppressWarnings("unused")
		boolean titreInconnu = false;
    	try {
    		artist = map.get("album artist").toString();
		} catch (NullPointerException e) {
			try{
				artist = map.get("artist").toString();
			}catch(NullPointerException ee)
			{
				
			}
		}
    	try {
    		year = " - " +  map.get("year").toString();
		} catch (NullPointerException e) {
			
		}
    	try {
    		album =" - " + map.get("album").toString();
		} catch (NullPointerException e) {
		
		}
    	try {
    		title = " - " +  map.get("title").toString();
		} catch (NullPointerException e) {
			
			title =getMediaFileTitle();
			titreInconnu = true;
		}
    	
    	if(album.equalsIgnoreCase(title))
    	{
    		lblFichier.setText(artist + year + album);
    	}
    	else
    	{
    		lblFichier.setText(artist + year + album + title);
    	}
    	
    }
    
    private String getMediaFileTitle()
    {
    	String[] pathMediaFile = mediaPlayer.getMedia().getSource().toString().split("/");
		String nomParDefaut = pathMediaFile[pathMediaFile.length-1];
		//System.out.println(nomParDefaut);
		String[] nomArray = nomParDefaut.split("\\.");
		//System.out.println(nomArray[0]);
		return " - ["+ nomArray[0].replace("%20", "")+"]";
    }
    
    private void afficherProgression(int progres_ds, int total_s) {
		progLecture.setProgress(progres_ds/10.0/total_s);  	    	
		int dixiemes = progres_ds % 10;
		int sec = progres_ds % 600 / 10;
		int min = progres_ds / 600;
		long secTot = total_s % 60;
		long minTot = total_s / 60;
		txtLecture.setText(String.format("%d:%02d.%d  /  %d:%02d", min, sec, dixiemes, minTot, secTot));
	}

    private void afficherProgressionAuBesoin(int progres_ms, int total_ms)
    {
    	int progres_ds = progres_ms / 100;
    	int total_s = total_ms / 1000;
    	if( lastProgres != progres_ds || lastTotal != total_s )
    	{
    		lastProgres = progres_ms;
    		lastTotal = total_s;
    		afficherProgression(progres_ds, total_s);
    	}
    }
    
    private void bindJouerPauseArret()
    {
		btnJouer.disableProperty().bind(
										mediaPlayer.statusProperty().isEqualTo(Status.PLAYING));
		btnPause.disableProperty().bind(
										mediaPlayer.statusProperty().isNotEqualTo(Status.PLAYING));
		btnArret.disableProperty().bind(
										mediaPlayer.statusProperty().isNotEqualTo(Status.PLAYING)
										.and((mediaPlayer.statusProperty().isNotEqualTo(Status.PAUSED))));
    }

    private void bindMediaPlayer(Media newMedia)
    {
		mediaPlayer = new MediaPlayer(newMedia);
		
		mediaPlayer.statusProperty().addListener(statusListener);
		
		mediaPlayer.setOnReady(()->{
										afficherMetadonnees();
										initProgression();
									});
    	
		bindJouerPauseArret();
    	
		// Volume
		mediaPlayer.volumeProperty().bind(sldrVolume.valueProperty().multiply(sldrVolume.valueProperty()).multiply(sldrVolume.valueProperty()));
    	
    	mediaPlayer.setOnEndOfMedia(()->{
								    		mediaPlayer.seek(Duration.ZERO);   			
								    		btnArret.fire();
								    		Platform.runLater(()->onEndOfMedia.run());
								    	});
    }
    
    private void initActions()
    {
    	// Actions des boutons
    	btnJouer.setOnAction(e->{
        	mediaPlayer.play();
			requestFocus(btnPause);
    	});
    	btnPause.setOnAction(e->{
    		mediaPlayer.pause();
    		requestFocus(btnJouer);
    	});
    	btnArret.setOnAction(e->{
    		mediaPlayer.stop();
    		requestFocus(btnJouer);
     	});

    	// Boutons forward / backward
    	btnForward.setOnAction(e->{
    		mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(30)));
    	});
    	
    	btnBackward.setOnAction(e->{
    		mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(30)));
    	});
    }

    @FXML
    private void initialize()
    {
		txtVolume.textProperty().bind(sldrVolume.valueProperty().subtract(0.2).multiply(125).asString("%.0f%%"));
		config(null, null);
		initActions();
    	bindNoSong();
    }
    
    private void initProgression()
    {
    	mediaPlayer.currentTimeProperty().addListener(timeListener);

    	btnForward.disableProperty().bind(
    			mediaPlayer.statusProperty().isNotEqualTo(Status.PLAYING));
    	
    	btnBackward.disableProperty().bind(
    			mediaPlayer.statusProperty().isNotEqualTo(Status.PLAYING));
    	
    	updateProgression();
    	
    }
    
    private boolean isBinded(){ return mediaPlayer != null; }
    
    private void requestFocus(Node node)
    {
		Platform.runLater(()->Platform.runLater(()->Platform.runLater(()->node.requestFocus())));
    }

	private void unbindMediaPlayer()
    {
    	if(isBinded())
    	{
    		mediaPlayer.stop();
    		mediaPlayer.volumeProperty().unbind();
    		mediaPlayer.statusProperty().removeListener(statusListener);
    		mediaPlayer.currentTimeProperty().removeListener(timeListener);
    		mediaPlayer = null;
    	}
    }
    
    
    private void updateProgression()
    {
    	int progres_ms = (int) mediaPlayer.getCurrentTime().toMillis();
    	int total_ms = (int) mediaPlayer.getMedia().getDuration().toMillis();
    	afficherProgressionAuBesoin(progres_ms, total_ms);
    }

    @Override
	protected String getFxml() { return "PlayPane.fxml"; }

    @Override
	protected Parent getRoot() { return root; }

    @Override
	protected ContextMenu getTestMenu(){
    	return new ContextMenu(
    			new MenuItem("Bind Popcorn"){{ setOnAction(event->{ 
    		    	URL musique = getClass().getResource("/ressources/popcorn.mp3");
    		    	bindSong(new Media(musique.toExternalForm()), "Popcorn");
    		    	Platform.runLater(()->btnJouer.requestFocus());
    				}); }},
    			new MenuItem("Play Popcorn"){{ setOnAction(event->{ 
    		    	URL musique = getClass().getResource("/ressources/popcorn.mp3");
    		    	bindAndPlay(new Media(musique.toExternalForm()), "Popcorn");
    		    	Platform.runLater(()->btnJouer.requestFocus());
    				}); }},
    			new MenuItem("Play None"){{ setOnAction(event->{ 
    				bindNoSong(); 
    				}); }}
    	);
    }
    
	@Override
	protected String getTitle() { return "TP3 - PlayPane"; }


	public Media getBindeMedia() {
		return newMedia;
	}
    
}

