import { ng, routes } from 'entcore';
import * as controllers from './controllers';
import * as services from './services';
import * as directives from './directives';

for(let controller in controllers){
	ng.controllers.push(controllers[controller]);
}

for (let service in services) {
	ng.services.push(services[service]);
}

for (let directive in directives) {
	ng.directives.push(directives[directive]);
}


routes.define(function($routeProvider){
	$routeProvider
		.when('/form/:formKey', { action: 'respondPublicForm' })
		.otherwise({ action: 'e404' });
});