package com.oursaviorgames.backend.model.datastore;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.Date;

import com.oursaviorgames.backend.utils.TimeUtils;

import static com.oursaviorgames.backend.utils.Preconditions.checkNotNull;

/**
 * Game entity containing all relevant information about a game.
 * Users only see {@code GameBean} projection of a game.
 * <br>
 * <b>Datastore info:</b>
 * <pre>
 *     Entity is cached.
 *     Indexes: {@code datePublished}, {@code hotScore}, {@code published}.
 * </pre>
 */
@Entity
@Cache
public class Game implements Serializable {

    public static final String F_DatePublished = "EF_DatePublished";
    public static final String F_HotScore = "EF_HotScore";
    public static final String F_IsPublished = "EF_IsPublished";

    @Id     Long            EF_Id;                     // id
            Key<Developer>  EF_Developer;
            String          EF_Title;                  // Game title.
            String          EF_ShortDescription;       // Game description.
            Date            EF_DateUpdated;            // Date game was updated.
            Date            EF_DateAdded;              // Date game was added to server.
    @Index  Date            EF_DatePublished;          // Date game was published publicly.
    @Index  long            EF_HotScore     = 0;       // Calculated HotScore.
    @Index  boolean         EF_IsPublished  = false;   // Public published flag.
            int             EF_Version      = 0;       // Internal version code.
            int             EF_PlayCount    = 0;       // Total play-count of this game.
            long            EF_PlayTime     = 0l;      // Total play-time in seconds.
            boolean         EF_IsOffline    = false;   // is the game playable offline.
            String          EF_OriginUrl;              // Games original link

    /**
     * Required default constructor for Objectify.
     */
    @SuppressWarnings("unused")
    private Game() {
    }

    /**
     * Constructor for creating a new Game.
     *
     * By default sets the published status to false.
     * By default sets all votes to zero.
     *
     * @param id A previously allocated id.
     * @param developer Developer of this game. Cannot be null.
     * @param title Game title. Cannot be null.
     */
    public Game(Long id, Key<Developer> developer, String title) {
        EF_Id = checkNotNull(id, "Game id cannot be null");
        EF_Developer = checkNotNull(developer, "Developer cannot be null");
        EF_Title = checkNotNull(title, "Game title cannot be null");
        EF_DateAdded = TimeUtils.getCurrentTime();
        EF_DateUpdated = TimeUtils.getCurrentTime();
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Developer> getDeveloperKey() {
		return EF_Developer;
	}

	public long getId() {
		return EF_Id;
	}
	
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Game> getKey() {
		return Key.create(Game.class, EF_Id);
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public int getVersion() {
		return EF_Version;
	}
	
	public String getTitle() {
		return EF_Title;
	}
	
	public String getShortDescription() {
		return EF_ShortDescription;
	}
	
	public long getHotScore() {
		return EF_HotScore;
	}
	
	public Date getDateCreated() {
		return EF_DateAdded;
	}
	
	public Date getDateUpdated() {
		return EF_DateUpdated;
	}

    /**
     * Total amount of play time.
     * @return
     */
    public long getPlayTime() {
        return EF_PlayTime;
    }

    /**
     * Total amount of plays.
     * @return
     *
     * @see #updateStats(int, long)
     */
    public int getPlayCount() {
        return EF_PlayCount;
    }

    public Date getDatePublished() {
        return EF_DatePublished;
    }

    public boolean isPlayableOffline() {
        return EF_IsOffline;
    }

    public String getOriginUrl() {
        return EF_OriginUrl;
    }

    public void setOriginUrl(String url) {
        EF_OriginUrl = url;
    }

    public void setPlayableOffline(boolean offline) {
        EF_IsOffline = offline;
    }

    public void setShortDescription(String description) {
        EF_ShortDescription = description;
    }

    public void setVersion(int newVersion) {
        EF_Version = newVersion;
    }

    /**
     * Increments play-count and play-time.
     * @param playCount
     * @param playTime
     */
    public void updateStats(int playCount, long playTime) {
        this.EF_PlayCount += playCount;
        this.EF_PlayTime += playTime;
    }

    public void setHotScore(long hotScore) {
        this.EF_HotScore = hotScore;
    }

    /**
     * Whether this game is published or not.
     */
    public boolean isPublished() {
        return EF_IsPublished;
    }

	/**
	 * Sets the published flag and published date.
	 * This game can now appear in game listings.
	 */
	public void setPublished(boolean published) {
		EF_IsPublished = published;
        if (published) {
            EF_DatePublished = TimeUtils.getCurrentTime();
        }
	}

    public static Key<Game> createKeyFromId(long id) {
        return Key.create(Game.class, id);
    }

}
