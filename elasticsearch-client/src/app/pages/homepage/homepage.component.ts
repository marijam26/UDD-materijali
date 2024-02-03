import { Component } from '@angular/core';
import {FileService} from "../../services/file.service";

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent {
  selected:string = 'Zakon';
  sent:boolean=false;
  file:File|undefined;

  constructor(private fileService:FileService) {
  }

  fileUpload(event:any){
    console.log(event.files[0]);
    this.file = event.files[0];
  }

  sendFile(){
    console.log(this.file);
    if(this.file){
      this.fileService.parseFile(this.file).subscribe({
        next:(data)=>{
          console.log('ok')
        },error:(err)=>{
          console.log(err)
        }
      });
    }

  }
}
