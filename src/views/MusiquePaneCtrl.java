package views;

import java.io.File;
import java.net.URL;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.MediaUtil;
import utils.Preference;
import utils.WindowApp;

public class MusiquePaneCtrl extends WindowApp {

	 public static void main(String[] args) { launch(args); }

    @FXML
    private HBox root;

    @FXML
    private VBox controlPane;

    @FXML
    private Label lblMusique;

    @FXML
    private VBox playPaneSlot;    
    
	@FXML
    private CheckBox checkRejouer;
	
	@FXML
	private VBox vBoxAnimationGauche;
	  
	@FXML
	private VBox vBoxAnimationDroit;
	
	@FXML
    private Button buttonTimeline;
	
	private AnimationPaneCtrl animationPaneCtrl = null;
	private AnimationPaneCtrl animationPaneCtrl1 = null;
	private AnimationPaneCtrl animationPaneCtrl2 = null;
	private AnimationPaneCtrl animationPaneCtrl3 = null;

	private PlayPaneCtrl playPaneCtrl = null;
	
    private Media media = null;
    private static final int CONTROL_PANE_WIDTH = 240;    

    private void initAnimation()
    {
    	animationPaneCtrl = (new AnimationPaneCtrl()).loadView();
    	animationPaneCtrl1 = (new AnimationPaneCtrl()).loadView();
    	animationPaneCtrl2 = (new AnimationPaneCtrl()).loadView();
    	animationPaneCtrl3 = (new AnimationPaneCtrl()).loadView();
    	vBoxAnimationGauche.getChildren().add(animationPaneCtrl.getRoot());
    	vBoxAnimationGauche.getChildren().add(animationPaneCtrl1.getRoot());
    	vBoxAnimationDroit.getChildren().add(animationPaneCtrl2.getRoot());
    	vBoxAnimationDroit.getChildren().add(animationPaneCtrl3.getRoot());
    	
    	VBox.setVgrow(animationPaneCtrl.getRoot(), Priority.ALWAYS);
    	VBox.setVgrow(animationPaneCtrl1.getRoot(), Priority.ALWAYS);
    	VBox.setVgrow(animationPaneCtrl2.getRoot(), Priority.ALWAYS);
    	VBox.setVgrow(animationPaneCtrl3.getRoot(), Priority.ALWAYS);
    	//root.getChildren().add(animationPaneCtrl.getRoot());
    }
    
    private void initDragAndDrop()
    {
    	controlPane.setOnDragDropped( event -> {
	    	Dragboard db = event.getDragboard();
	        String erreur = null;
	        if (db.hasFiles())
	        {
	        	File f = db.getFiles().get(0);
	        	try {
		        	Media newMedia = new Media(f.toURI().toString());
		            
		        	if( newMedia.getError()!= null )
		        	{
		        		erreur = "Impossible de charger le fichier: " + f.getName();
		        	}
		        	else 
		        	{
		        		playPaneCtrl.bindSong(newMedia, f.getName());
		        	};
	        	}
	        	catch(MediaException e)
	        	{
	        		erreur = "Impossible de charger le fichier: " + f.getName() + " ( " + e.getMessage() + " )";
	        	}
	        }
	        if(erreur != null){
	        	Alert alert = new Alert(AlertType.ERROR, erreur);
	        	alert.showAndWait();        		        	
	        }
	        event.setDropCompleted(erreur != null);
			controlPane.getStyleClass().remove("DragOver");
	        event.consume();    	
	    });
    	
    	controlPane.setOnDragEntered( event -> {
	    	if (event.getDragboard().hasFiles()) 
	    	{
	    		controlPane.getStyleClass().add("DragOver");
	            event.consume();    	
	        }
	    });
    	
    	controlPane.setOnDragExited( event -> {
			controlPane.getStyleClass().remove("DragOver");
			event.consume();    	
	    });
    	
    	controlPane.setOnDragOver( event -> {
			if ( event.getDragboard().hasFiles() ) 
			{
	            event.acceptTransferModes(TransferMode.COPY);
	            event.consume();
	        }
	    });
   	
    }

    @FXML
    private void initialize()
    {
    	initAnimation();
    	initPlayPane();
    	initDragAndDrop();
    	initSize();
    }
   
    @FXML
    void actionTimeLine(ActionEvent event) {
    	double largeurActuelle = getStage().getWidth();
    	double largeurCible = largeurActuelle > largeurIdealeDuStageSansAnimation() ? largeurIdealeDuStageSansAnimation() : this.largeurIdealeDuStage();
    	double opaciry = largeurActuelle > largeurIdealeDuStageSansAnimation() ? 0 : 1;
    	
    	
    	DoubleProperty stageWidthProxyProperty = new SimpleDoubleProperty(largeurActuelle);
    	stageWidthProxyProperty.addListener(dontcare -> 
    	{
    		getStage().setWidth(stageWidthProxyProperty.get());
    		
    	});
    	
    	Timeline timeline = new Timeline(
    									new KeyFrame(Duration.millis(0), new KeyValue(lblMusique.translateYProperty(), 0)),
    																	
    									new KeyFrame(Duration.millis(1000),	new KeyValue(lblMusique.translateYProperty(), 50)),
    									
    									new KeyFrame(Duration.millis(2000), new KeyValue(lblMusique.translateYProperty(), 0), 
    																		new KeyValue(lblMusique.scaleXProperty(), 1)),
    									
    									new KeyFrame(Duration.millis(3000), new KeyValue(lblMusique.scaleXProperty(), 1.5)),
    									
    									new KeyFrame(Duration.millis(4000), new KeyValue(lblMusique.scaleXProperty(), 1), 
    																		new KeyValue(stageWidthProxyProperty, largeurCible), 
    																		new KeyValue(animationPaneCtrl.getRoot().opacityProperty(), opaciry),
    																		new KeyValue(animationPaneCtrl1.getRoot().opacityProperty(), opaciry),
    																		new KeyValue(animationPaneCtrl2.getRoot().opacityProperty(), opaciry),
    																		new KeyValue(animationPaneCtrl3.getRoot().opacityProperty(), opaciry))
    									
    									);
    	timeline.setOnFinished(dontcare ->System.out.println("Timeline terminé"));
    	timeline.playFromStart();
    	timeline.setAutoReverse(true);
    	System.out.println("timeline démarée");
    	
    	
    }
    
    private double largeurIdealeDuStageSansAnimation()
    {
    	return CONTROL_PANE_WIDTH + root.getPadding().getLeft() + root.getPadding().getRight();
    }
    /**
     * Méthode qui initialise la taille de la fenêtre
     */
    private void initSize()
    {
    	controlPane.setMinHeight(CONTROL_PANE_WIDTH);
    	controlPane.setMinWidth(CONTROL_PANE_WIDTH);
    	
    	root.sceneProperty().addListener(change ->{
    		//maintenant on connait la scene
    		root.getScene().windowProperty().addListener(changeScene ->{
    			//maintenant on connait le stage
    			Rectangle2D screenSize= Screen.getPrimary().getVisualBounds();
    	    	getStage().setHeight(screenSize.getHeight() * 0.9);	
    	    	getStage().setWidth(largeurIdealeDuStage());
    		});
    	});
    }
    
    private Stage getStage()
    {
    	return (Stage) root.getScene().getWindow();
    }

    private double largeurIdealeDuStage()
    {
    	double largeurIdeale = CONTROL_PANE_WIDTH + getStage().getHeight();
    	double largeurEcran = Screen.getPrimary().getVisualBounds().getWidth();
    	return Math.min(largeurIdeale, largeurEcran * 0.9);
    }
    
    private void initPlayPane()
    {
    	//URL musique = getClass().getResource("/ressources/popcorn.mp3");
    	URL musique = getClass().getResource("/ressources/ACDC - Back In Black.mp3");
    	media = new Media(musique.toExternalForm());
    	
    	playPaneCtrl = (new PlayPaneCtrl()).loadView();
    	playPaneSlot.getChildren().add(playPaneCtrl.getRoot());
    	VBox.setVgrow(playPaneCtrl.getRoot(), Priority.SOMETIMES);
    	playPaneCtrl.config( 
    			newStatus->onPlayingStatusChange(newStatus), 
    			()->onEndOfMedia());
    	playPaneCtrl.bindSong(media, "Back In Black");
    	animationPaneCtrl.bindRate(playPaneCtrl.volumeProperty());
    	animationPaneCtrl1.bindRate(playPaneCtrl.volumeProperty());
    	animationPaneCtrl2.bindRate(playPaneCtrl.volumeProperty());
    	animationPaneCtrl3.bindRate(playPaneCtrl.volumeProperty());
    }
    
    private void onEndOfMedia()
    {
		if(checkRejouer.isSelected()) playPaneCtrl.bindAndPlay(media, "Back In Black"); 
    }
    
    private void onPlayingStatusChange(Status newStatus)
    {
		if(newStatus == Status.PLAYING) {
			animationPaneCtrl.play();
			animationPaneCtrl1.play();
			animationPaneCtrl2.play();
			animationPaneCtrl3.play();
		}
		else {
			animationPaneCtrl.pause();
			animationPaneCtrl1.pause();
			animationPaneCtrl2.pause();
			animationPaneCtrl3.pause();
		}
    }

    public void stockerPreferences()
    {
    	Preference pref = new Preference(){{
    		statgeWidth = getStage().getWidth();
    		stageHeight = getStage().getHeight();
    		Media media = playPaneCtrl.getBindeMedia();
    		songFile = media == null ? null : media.getSource();
    		volume = playPaneCtrl.getSldrVolume().getValue();
    		checkRejouerSelect = checkRejouer.selectedProperty().getValue();
    	}};
    	pref.stoker();    			
    }
    
    @Override
	protected String getFxml() { return "MusiquePane.fxml"; }

    @Override
	protected Parent getRoot() { return root; }
    
	@Override
	protected String getTitle() { return "TP3 - Musique - Adrian Pinzaru"; }
	
	@Override
	protected ContextMenu getTestMenu()
	{
		return new ContextMenu(
					new MenuItem("Souver preferences"){{setOnAction(event->stockerPreferences());}},
					new MenuItem("Charger preferences"){{setOnAction(event->chargerPreferences(getStage()));}},
					new MenuItem("Unbind Media"){{setOnAction(event->playPaneCtrl.bindNoSong());}},
					new MenuItem("Quitter"){{setOnAction(event->{System.exit(0);});}}
					);
	}
	
	public void chargerPreferences(Stage stage)
	{
		Preference pref = Preference.charger();
		if(pref == null) return;
		
		if(pref.stageHeight > 0)  stage.setHeight(Math.min(pref.stageHeight, Screen.getPrimary().getVisualBounds().getHeight()));
		if(pref.statgeWidth > 0)  stage.setWidth(Math.min(pref.statgeWidth, Screen.getPrimary().getVisualBounds().getWidth()));
		
		if(pref.songFile != null) bindSongFile(pref.songFile);
		else playPaneCtrl.bindNoSong();
		
		playPaneCtrl.getSldrVolume().setValue(pref.volume);
		checkRejouer.setSelected(pref.checkRejouerSelect);
	}

	private void bindSongFile(String songFile) {
		Preference pref = Preference.charger();
		Media newMedia = new Media(pref.songFile);
		playPaneCtrl.bindSong(newMedia, MediaUtil.getMediaFileName(newMedia));
	}
}

