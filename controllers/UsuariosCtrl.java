package controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class UsuariosCtrl {

  private final EventBus eb;
  // Claves válidas para un Usuario
  private final String[] USUARIO_SCHEMA = { "nombre", "apellidos" };

  /**
   * Recupera la lista de usuarios filtrando por nombre y apellidos
   */
  public Handler<HttpServerRequest> query = new Handler<HttpServerRequest>() {
    public void handle(final HttpServerRequest req) {
      String nombre = req.params().get("nombre");
      String apellidos = req.params().get("apellidos");
      JsonObject matcher = new JsonObject();
      if (nombre != null) {
        matcher.putObject("nombre",
            new JsonObject()
              .putString("$regex", nombre)
              .putString("$options", "i"));
      }
      if (apellidos != null) {
        matcher.putObject("apellidos",
            new JsonObject()
              .putString("$regex", apellidos)
              .putString("$options", "i"));
      }
      JsonObject sortObj = new JsonObject().putNumber("apellidos", 1);
      JsonObject json = new JsonObject()
        .putString("collection", "usuarios")
        .putString("action", "find")
        .putObject("matcher", matcher)
        .putObject("sort", sortObj);
      eb.send("vertx.mongopersistor", json,
          new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> reply) {
              JsonArray results = reply.body().getArray("results");
              req.response().end(results.encode());
            }
          });
    }
  };

  /**
   * Recupera un usuario
   */
  public Handler<HttpServerRequest> get = new Handler<HttpServerRequest>() {
    public void handle(final HttpServerRequest req) {
      JsonObject matcher = new JsonObject()
        .putString("_id", req.params().get("id"));
      JsonObject json = new JsonObject()
        .putString("collection", "usuarios")
        .putString("action", "findone")
        .putObject("matcher", matcher);
      eb.send("vertx.mongopersistor", json,
          new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> reply) {
              JsonObject result = reply.body().getObject("result");
              req.response().end(result.encode());
            }
          });
    }
  };

  /**
   * Añade un nuevo usuario
   */
  public Handler<HttpServerRequest> add = new Handler<HttpServerRequest>() {
    public void handle(final HttpServerRequest req) {
      req.bodyHandler(new Handler<Buffer>() {
        public void handle(Buffer body) {
          JsonObject bodyJson = new JsonObject(body.toString());
          final JsonObject usuario = validateSchema(bodyJson, USUARIO_SCHEMA);
          formateaPropiedades(usuario);

          JsonObject json = new JsonObject()
            .putString("collection", "usuarios")
            .putString("action", "save")
            .putObject("document", usuario);

          eb.send("vertx.mongopersistor", json,
              new Handler<Message<JsonObject>>() {
                public void handle(Message<JsonObject> reply) {
                  req.response().end(usuario.encode());
                }
              });
        }
      });
    }
  };

  /**
   * Elimina un usuario
   */
  public Handler<HttpServerRequest> delete = new Handler<HttpServerRequest>() {
    public void handle(final HttpServerRequest req) {
      JsonObject matcher = new JsonObject()
        .putString("_id", req.params().get("id"));
      JsonObject json = new JsonObject()
        .putString("collection", "usuarios")
        .putString("action", "delete")
        .putObject("matcher", matcher);
      eb.send("vertx.mongopersistor", json,
          new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> reply) {
              req.response().end();
            }
          });
    }
  };

  /**
   * Actualiza un usuario
   */
  public Handler<HttpServerRequest> update = new Handler<HttpServerRequest>() {
    public void handle(final HttpServerRequest req) {
      req.bodyHandler(new Handler<Buffer>() {
        public void handle(Buffer body) {
          JsonObject bodyJson = new JsonObject(body.toString());
          final JsonObject usuario = validateSchema(bodyJson, USUARIO_SCHEMA);
          formateaPropiedades(usuario);

          JsonObject criteria = new JsonObject()
            .putString("_id", req.params().get("id"));
          JsonObject json = new JsonObject()
            .putString("collection", "usuarios")
            .putString("action", "update")
            .putObject("criteria", criteria)
            .putObject("objNew", usuario);

          eb.send("vertx.mongopersistor", json,
              new Handler<Message<JsonObject>>() {
                public void handle(Message<JsonObject> reply) {
                  req.response().end(usuario.encode());
                }
              });
        }
      });
    }
  };

  public UsuariosCtrl(EventBus eb) {
    this.eb = eb;
  }

  /**
   * Devuelve una copia del objecto JSON pasado por parámetro, pero que
   * contiene únicamente las claves indicadas por el parámetro schema
   */
  private JsonObject validateSchema(JsonObject json, String[] schema) {
    Map<String, Object> inMap = json.toMap();
    Map<String, Object> outMap = new HashMap<String, Object>();
    for (String key : inMap.keySet()) {
      if (Arrays.asList(schema).contains(key)) {
        outMap.put(key, inMap.get(key));
      }
    }
    return new JsonObject(outMap);
  }

  /**
   * Da formato a las propiedades de un usario
   */
  private void formateaPropiedades(JsonObject usuario) {
    String nombre = usuario.getString("nombre");
    String apellidos = usuario.getString("apellidos");
    if (nombre != null) {
      usuario.putString("nombre", titleize(nombre));
    }
    if (apellidos != null) {
      usuario.putString("apellidos", titleize(apellidos));
    }
  }

  /**
   * Devuelve un string similar al de entrada pero con la primera letra de
   * cada palabra en mayúscula y el resto en minúscula
   */
  private String titleize(String source){
    source = source.toLowerCase();
    boolean cap = true;
    char[]  out = source.toCharArray();
    int i, len = source.length();
    for(i=0; i<len; i++){
      if(Character.isWhitespace(out[i])){
        cap = true;
        continue;
      }
      if(cap){
        out[i] = Character.toUpperCase(out[i]);
        cap = false;
      }
    }
    return new String(out);
  }
}
