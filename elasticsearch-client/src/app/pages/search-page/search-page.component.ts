import { Component } from '@angular/core';
import {SearchService} from "../../services/search.service";
import {LocationDTO} from "../../models/location";
import {FileService} from "../../services/file.service";

@Component({
  selector: 'app-search-page',
  templateUrl: './search-page.component.html',
  styleUrls: ['./search-page.component.css']
})
export class SearchPageComponent {
  phrase:string='';
  tokens:string[]=[];
  result = []
  highlighters = [];
  location:LocationDTO = new LocationDTO();

  constructor(private searchService:SearchService, private fileService:FileService) {
  }

  tokenizeQuery(query: string): string[] {
    const tokens: string[] = [];
    let currentToken = '';

    for (let i = 0; i < query.length; i++) {
      const char = query[i];

      if (char === "'" || char === '"') {
        // Skip special characters and spaces
        continue;
      }

      if (char === 'A' && query.substring(i, i + 3) === 'AND') {
        // Handle AND as a separate token
        if (currentToken !== '') {
          tokens.push(currentToken.trim());
          currentToken = '';
        }
        tokens.push('AND');
        i += 2; // Skip the next two characters ('N' and 'D')
      } else if (char === 'O' && query.substring(i, i + 2) === 'OR') {
        if (currentToken !== '') {
          tokens.push(currentToken.trim());
          currentToken = '';
        }
        tokens.push('OR');
        i += 1; // Skip the next character ('R')
      } else if (char === 'N' && query.substring(i, i + 3) === 'NOT') {
        // Handle NOT as a separate token
        if (currentToken !== '') {
          tokens.push(currentToken.trim());
          currentToken = '';
        }
        tokens.push('NOT');
        i += 2; // Skip the next two characters ('O' and 'T')
      } else {
        // Add characters to the current token
        currentToken += char;
      }
    }

    // Add the last token
    if (currentToken !== '') {
      tokens.push(currentToken.trim());
    }

    return tokens;
  }

  search(){
    this.tokens = this.tokenizeQuery(this.phrase);
    console.log(this.tokens);

    if (this.tokens.includes('AND') || this.tokens.includes('OR') || this.tokens.includes('NOT')){
      this.searchService.advancedSearchFile(this.tokens).subscribe(
        {
          next:(data) => {
            console.log(data)
            this.result = data.pages.content
            this.highlighters = data.highlighters;
            alert('ok');
          }, error:(err) => {
            console.log(err)
          }
        });
    }else{
      this.searchService.simpleSearchFile(this.tokens).subscribe(
        {
          next:(data) => {
            console.log(data)
            this.result = data.pages.content;
            this.highlighters = data.highlighters;
            alert('ok');
          }, error:(err) => {
            console.log(err)
          }
        });
    }
  }

  searchGeo(){
    this.searchService.geoSearchFile(this.location).subscribe(
      {
        next:(data) => {
          console.log(data)
          this.result = data.pages.content;
          this.highlighters = data.highlighters;
          alert('ok');
        }, error:(err) => {
          console.log(err)
        }
      }
    );
  }

  open(path:string){
    this.fileService.downloadFile(path).subscribe(data => {
      console.log(data)
      const url = window.URL.createObjectURL(new Blob([data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", path);
      document.body.appendChild(link);
      link.click();
    });
  }
}
