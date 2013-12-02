'use strict';

app.controller('UsuariosCtrl', function ($scope, $timeout, Usuarios) {

  /**
   * Añade una alerta y la muestra durante 2s
   */
  var showAlert = function (type, msg) {
    $scope.alerts.push({ type: type, msg: msg });
    $timeout(function () {
      $scope.alerts.shift();
    }, 2000);
  };

  $scope.alerts = [];

  /**
   * Recupera un usuario por id
   */
  $scope.getUsuario = function (id) {
    $scope.usuario = Usuarios.get({ id: id });
  };

  /**
   * Busca usuarios
   */
  $scope.queryUsuarios = function () {
    Usuarios.query(
      $scope.filtro,
      function (usuarios) {
        $scope.usuarios = usuarios;
      }, function () {
        showAlert('danger', 'Error al recuperar los usuarios');
      });
  };

  /**
   * Añade un usuario
   */
  $scope.addUsuario = function () {
    Usuarios.save(
      $scope.nuevoUsuario,
      function (usuario) {
        // Muestra mensaje de éxito
        showAlert('success', 'Se ha creado con éxito el usuario: ' +
                  (usuario.nombre || '') + ' ' + (usuario.apellidos || ''));
        // Borra los inputs del formulario utilizado para la creación
        $scope.nuevoUsuario = null;
      }, function () {
        showAlert('danger', 'Error al añadir el usuario');
      });
  };

  /**
   * Elimina un usuario
   */
  $scope.deleteUsuario = function (usuario) {
    usuario.$delete(function () {
      // Elimina el usuario del array de usuarios
      var index = $scope.usuarios.indexOf(usuario);
      $scope.usuarios.splice(index, 1);
      showAlert('success', 'Se ha eliminado con éxito el usuario: ' +
                (usuario.nombre || '') + ' ' + (usuario.apellidos || ''));
    }, function () {
      showAlert('danger', 'Error al eliminar el usuario');
    });
  };

  /**
   * Selecciona un usuario para su edición
   */
  $scope.editUsuario = function (usuario) {
    if (usuario.edicion) {
      return;
    }
    // Crea una copia del usuario seleccionado para editarlo
    usuario.edicion = angular.copy(usuario);
  };

  /**
   * Actualiza el usuario en el servidor
   */
  $scope.updateUsuario = function (usuario) {
    Usuarios.update(
      usuario.edicion,
      function (usuarioActualizado) {
        // Finaliza la edición
        usuario.edicion = null;
        // Actualiza el usuario con el devuelto por el servidor
        _.extend(usuario, usuarioActualizado);
        // Muestra mensaje de éxito
        showAlert('success', 'Se ha actualizado con éxito el usuario: ' +
                  (usuario.nombre || '') + ' ' + (usuario.apellidos || ''));
      },
      function () {
        showAlert('danger', 'Error al actualizar el usuario');
      });
  };
});
