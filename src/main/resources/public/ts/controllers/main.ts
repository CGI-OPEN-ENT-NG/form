import {Behaviours, idiom, model, ng, template} from 'entcore';
import {Form, QuestionTypes} from "../models";
import {formService} from "../services";

export const mainController = ng.controller('MainController', ['$scope', 'route', '$location', 'FormService',
	($scope, route, $location) => {
		$scope.lang = idiom;
		$scope.template = template;

		// Init variables
		$scope.currentTab = 'formsList';
		$scope.edit = {
			mode: false,
			form: new Form()
		};
		$scope.questionTypes = new QuestionTypes();
		$scope.questionTypes.sync();

		// Routing & template opening

		route({
			list: () => {
				if ($scope.canCreate()) {
					$scope.redirectTo('/list/mine');
				}
				else if ($scope.canRespond()) {
					$scope.redirectTo('/list/responses');
				}
				else {
					$scope.redirectTo('/e403');
				}
			},
			formsList: () => {
				$scope.currentTab = 'formsList';
				template.open('main', 'containers/forms-list');
			},
			formsResponses: () => {
				$scope.currentTab = 'formsResponses';
				template.open('main', 'containers/forms-responses');
			},
			createForm: () => {
				template.open('main', 'containers/create-form');
			},
			openForm: async (params) => {
				if ($scope.canCreate()) {
					let { data } = await formService.get(params.idForm);
					$scope.edit.form = data;
					$scope.edit.mode = true;
					template.open('main', 'containers/edit-form');
				}
				else if ($scope.canRespond()) {
					// TODO open view response to form
				}
				else {
					$scope.redirectTo('/e403');
				}
			},
			e403: () => {
				template.open('main', 'containers/e403');
			},
			e404: () => {
				template.open('main', 'containers/e404');
			}
		});

		// Utils

		$scope.getTypeNameByCode = (code: number) : string => {
			return $scope.questionTypes.all.filter(type => type.code === code)[0].name;
		};

		$scope.redirectTo = (path: string) => {
			$location.path(path);
		};

		$scope.safeApply = (fn?) => {
			const phase = $scope.$root.$$phase;
			if (phase == '$apply' || phase == '$digest') {
				if (fn && (typeof (fn) === 'function')) {
					fn();
				}
			} else {
				$scope.$apply(fn);
			}
		};

		$scope.hasRight = (right: string) => {
			return model.me.hasWorkflow(right);
		};

		$scope.hasAccess = () => {
			return $scope.hasRight(Behaviours.applicationsBehaviours.formulaire.rights.workflow.access);
		};

		$scope.canCreate = () => {
			return $scope.hasRight(Behaviours.applicationsBehaviours.formulaire.rights.workflow.creation);
		};

		$scope.canRespond = () => {
			return $scope.hasRight(Behaviours.applicationsBehaviours.formulaire.rights.workflow.response);
		};

		// $scope.canSend = () => {
		// 	return $scope.hasRight(Behaviours.applicationsBehaviours.formulaire.rights.workflow.sending);
		// };
		//
		// $scope.canShare = () => {
		// 	return $scope.hasRight(Behaviours.applicationsBehaviours.formulaire.rights.workflow.sharing);
		// };
}]);
