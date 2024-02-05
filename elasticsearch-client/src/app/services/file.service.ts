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

  downloadFile(path:string){
    const newUrl = this.url + '/download/'+path;
    return this._http.get<Blob>(newUrl, {
      headers: new HttpHeaders({'Content-Type': 'application/json' }),
      responseType: 'blob' as 'json'
    })
  }

}
