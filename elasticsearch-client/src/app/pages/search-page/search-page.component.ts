import { Component } from '@angular/core';
import {SearchService} from "../../services/search.service";

@Component({
  selector: 'app-search-page',
  templateUrl: './search-page.component.html',
  styleUrls: ['./search-page.component.css']
})
export class SearchPageComponent {
  phrase:string='';
  tokens:string[]=[];
  result=[]

  constructor(private searchService:SearchService) {
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
            console.log(data.content)
            this.result = data.content
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
            alert('ok');
          }, error:(err) => {
            console.log(err)
          }
        });
    }
  }

  onChange(){
    if (this.phrase.split(' ').includes('AND')){

    }
  }
}
