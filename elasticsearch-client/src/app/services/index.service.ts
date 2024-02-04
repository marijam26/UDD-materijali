import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class IndexService {

  constructor(private _http: HttpClient) { }

  url = 'http://localhost:8081/api/index';

  indexFile(file:File){
    const fd = new FormData();
    fd.append('file', file);
    return this._http.post<any>(this.url, fd, {
      headers: new HttpHeaders({
        'Access-Control-Allow-Origin': '*',
      }),
    });
  }
}