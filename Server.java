import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import controllers.UsuariosCtrl;

public class Server extends Verticle {

  private final static String API_ROOT = "/api";
  private final static String WEB_ROOT = "public/";

  public void start() {

    final Logger logger = container.logger();
    final EventBus eb = vertx.eventBus();

    // Despliega el módulo de Mongodb
    JsonObject config = container.config();
    container.deployModule("io.vertx~mod-mongo-persistor~2.1.0",
        config.getObject("db"));

    // Rutas del servicio
    UsuariosCtrl usuarios = new UsuariosCtrl(eb);
    RouteMatcher rm = new RouteMatcher();

    rm.get(API_ROOT + "/usuarios", usuarios.query);
    rm.get(API_ROOT + "/usuarios/:id", usuarios.get);
    rm.post(API_ROOT + "/usuarios", usuarios.add);
    rm.put(API_ROOT + "/usuarios/:id", usuarios.update);
    rm.delete(API_ROOT + "/usuarios/:id", usuarios.delete);

    // Sirve documentos estáticos (html, css, js...)
    rm.getWithRegEx(".*", new Handler<HttpServerRequest>() {
      public void handle(HttpServerRequest req) {
        if (req.path().equals("/")) {
          req.response().sendFile(WEB_ROOT + "index.html");
        } else {
          req.response().sendFile(WEB_ROOT + req.path());
        }
      }
    });

    // Inicia el servidor
    String envPort = System.getenv("PORT");
    int port = envPort == null ? 8080 : Integer.valueOf(envPort);
    vertx.createHttpServer().requestHandler(rm).listen(port, "0.0.0.0");
    logger.info("Servidor iniciado en http://0.0.0.0:" + port);
  }
}
