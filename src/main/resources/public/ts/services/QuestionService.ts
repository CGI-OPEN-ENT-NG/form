import {idiom, ng, notify} from 'entcore';
import http, {AxiosResponse} from 'axios';
import {Question} from "../models";

export interface QuestionService {
    list(number): Promise<AxiosResponse>;
    create(Question): Promise<AxiosResponse>;
    get(number): Promise<AxiosResponse>;
    update(Question): Promise<AxiosResponse>;
    delete(number): Promise<AxiosResponse>;
}

export const questionService: QuestionService = {

    async list (formId : number): Promise<AxiosResponse> {
        try {
            return await http.get(`/formulaire/forms/${formId}/questions`);
        } catch (err) {
            notify.error(idiom.translate('formulaire.error.questionService.list'));
            throw err;
        }
    },

    async create(question : Question): Promise<AxiosResponse> {
        try {
            return await http.post(`/formulaire/forms/${question.form_id}/questions`, question);
        } catch (err) {
            notify.error(idiom.translate('formulaire.error.questionService.create'));
            throw err;
        }
    },

    async get(id : number): Promise<AxiosResponse> {
        try {
            return await http.get(`/formulaire/questions/${id}`);
        } catch (err) {
            notify.error(idiom.translate('formulaire.error.questionService.get'));
            throw err;
        }
    },

    async update(question : Question): Promise<AxiosResponse> {
        try {
            return await http.post(`/formulaire/questions/${question.id}`);
        } catch (err) {
            notify.error(idiom.translate('formulaire.error.questionService.update'));
            throw err;
        }
    },

    async delete(id : number): Promise<AxiosResponse> {
        try {
            return await http.delete(`/formulaire/questions/${id}`);
        } catch (err) {
            notify.error(idiom.translate('formulaire.error.questionService.delete'));
            throw err;
        }
    }
};

export const QuestionService = ng.service('QuestionService', (): QuestionService => questionService);