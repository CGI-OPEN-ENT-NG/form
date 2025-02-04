import {ng} from 'entcore';
import {distributionService, formService} from "../services";
import {Form} from "../models";
import {FORMULAIRE_BROADCAST_EVENT} from "@common/core/enums";
import {Delegates} from "../models";
import {Folder} from "../models";

interface ViewModel {
    form: Form;
    folder: Folder;
    delegates: Delegates;
    rgpdLifetimeChoices: number[];
    display: {
        date_ending: boolean
    }

    $onInit() : Promise<void>;
    saveGuard(): void;
    save() : Promise<void>;
    checkIntervalDates() : boolean;
    getImage() : void;
    onSwitchPublic(newValue: boolean): void;
}


export const formPropController = ng.controller('FormPropController', ['$scope',
    function ($scope) {

        const vm: ViewModel = this;
        vm.form = new Form();
        vm.folder = new Folder();
        vm.delegates = new Delegates();
        vm.display = {
            date_ending: false
        };

        vm.$onInit = async () : Promise<void> => {
            vm.form = $scope.form;
            vm.folder = $scope.folder;
            vm.display.date_ending = !!vm.form.date_ending;
            await vm.delegates.sync();
            vm.form.nb_responses = vm.form.id ? (await distributionService.count(vm.form.id)).count : 0;
            vm.form.folder_id = vm.folder.id;
            vm.rgpdLifetimeChoices = [3, 6, 9, 12];
            $scope.safeApply();
        };

        // Functions

        vm.saveGuard = () => {
            vm.save().then();
        };

        vm.save = async () : Promise<void> => {
            if (vm.form.title && vm.checkIntervalDates()) {
                vm.form = await formService.save(vm.form);
                $scope.redirectTo(`/form/${vm.form.id}/edit`);
                $scope.safeApply();
            }
        };

        vm.checkIntervalDates = () : boolean => {
            if (vm.display.date_ending) {
                if (!vm.form.date_ending) {
                    vm.form.date_ending = new Date(vm.form.date_opening);
                    vm.form.date_ending.setFullYear(vm.form.date_ending.getFullYear() + 1);
                }
                return vm.form.date_ending > vm.form.date_opening;
            }
            else {
                vm.form.date_ending = null;
                return true;
            }

        };

        vm.getImage = async () : Promise<void> => {
            if (vm.form.picture) {
                await vm.form.setInfoImage();
                // window.setTimeout(function() {
                //     if(!vm.form.infoImg.compatible) {
                //         notify.error(idiom.translate('formulaire.image.incompatible'));
                //     }
                // }, 2000)
            }
            $scope.safeApply();
        };

        vm.onSwitchPublic = (newValue: boolean) : void => {
            if (newValue) {
                vm.display.date_ending = true;
                vm.checkIntervalDates();
                vm.form.multiple = false;
                vm.form.editable = false;
                vm.form.anonymous = true;
            }
        };

        $scope.$on(FORMULAIRE_BROADCAST_EVENT.INIT_FORM_PROP, () => { vm.$onInit() });
    }]);