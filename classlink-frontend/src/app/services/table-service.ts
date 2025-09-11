import {computed, Injectable} from '@angular/core';
import {environment} from "../../environments/environment.development";

@Injectable({
    providedIn: 'root'
})
export class TableService {
    restApi = computed(() => `${environment.API_URL}/audit-logs`)
    constructor() {
    }
}
