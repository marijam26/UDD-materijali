import { Injectable } from '@angular/core';
import {HttpClient,HttpHeaders} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private _http: HttpClient) { }

  url = 'http://localhost:8081/api/file';

  parseFile(file:File){
    const fd = new FormData();
    fd.append('file', file);
    const newUrl = this.url + '/parse';
    return this._http.post<any>(newUrl, fd, {
      headers: new HttpHeaders({
        'Access-Control-Allow-Origin': '*',
      }),
    });
  }

}
