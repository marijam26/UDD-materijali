import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {LocationDTO} from "../models/location";

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

  geoSearchFile(dto:LocationDTO){
    return this._http.post<any>(this.url + '/geo', dto, {
      headers: new HttpHeaders({
        'Access-Control-Allow-Origin': '*',
      }),
    });
  }

}
