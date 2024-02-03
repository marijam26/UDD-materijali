import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private _http: HttpClient) { }

  url = 'http://localhost:8081/api/file';

  parseFile(file:File){
    const newUrl = this.url + '/parse';
    return this._http.post<any>(newUrl, file);
  }
}
