import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class IndexService {

  constructor(private _http: HttpClient) { }

  url = 'http://localhost:8081/api/index';

  indexFile(file:File, type:string){
    const fd = new FormData();
    fd.append('file', file);
    fd.append('type', type);
    return this._http.post<any>(this.url, fd, {
      headers: new HttpHeaders({
        'Access-Control-Allow-Origin': '*',
      }),
    });
  }
}
