import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  constructor(private _http: HttpClient) { }

  url = 'http://localhost:8081/api/search';

  simpleSearchFile(keywords:string[]){
    return this._http.post<any>(this.url + '/simple', {'keywords':keywords}, {
      headers: new HttpHeaders({
        'Access-Control-Allow-Origin': '*',
      }),
    });
  }

  advancedSearchFile(keywords:string[]){
    return this._http.post<any>(this.url + '/advanced', {'keywords':keywords}, {
      headers: new HttpHeaders({
        'Access-Control-Allow-Origin': '*',
      }),
    });
  }

}
