package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;

    private PlatformTransactionManager albumTransactionManager;
    private  PlatformTransactionManager movieTransactionManager;

    @Autowired
    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures,
                          @Qualifier("movieTransactionManager") PlatformTransactionManager movieTransactionManager,@Qualifier("albumTransactionManager") PlatformTransactionManager albumTransactionManager) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.movieTransactionManager=movieTransactionManager;
        this.albumTransactionManager=albumTransactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        // explicitly setting the transaction name is something that can be done only programmatically
        def.setName("Movie Txn");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//
        TransactionStatus status = movieTransactionManager.getTransaction(def);
        try {
            for (Movie movie : movieFixtures.load()) {
                moviesBean.addMovie(movie);
            }

            model.put("movies", moviesBean.getMovies());
        }
        catch (Exception ex) {
            movieTransactionManager.rollback(status);
            throw ex;
        }
        movieTransactionManager.commit(status);

        DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
        // explicitly setting the transaction name is something that can be done only programmatically
        def2.setName("Album Txn");
        def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//
        TransactionStatus status2 = albumTransactionManager.getTransaction(def2);
        try {
            for (Album album : albumFixtures.load()) {
                albumsBean.addAlbum(album);
            }
            model.put("albums", albumsBean.getAlbums());
        }
        catch (Exception ex) {
            albumTransactionManager.rollback(status2);
            throw ex;
        }
        albumTransactionManager.commit(status2);

        return "setup";
    }
}
