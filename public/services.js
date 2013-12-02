'use strict';

app.factory('Usuarios', function ($resource) {
  return $resource(
    '/api/usuarios/:id', {
    id: '@_id'
  }, {
    update: {
      method: 'PUT',
      data: {},
      isArray: false
    }
  });
});
