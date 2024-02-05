import { Component } from '@angular/core';
import {FileService} from "../../services/file.service";
import {AgencyContractValues} from "../../models/agencyContractValues";
import {IndexService} from "../../services/index.service";

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent {
  selected:string = 'Zakon';
  sent:boolean=false;
  file:File|undefined;
  contract:AgencyContractValues = new AgencyContractValues();

  constructor(private fileService:FileService, private indexService:IndexService) {
  }

  fileUpload(event:any){
    console.log(event.files[0]);
    this.file = event.files[0];
  }

  sendFile(){
    if(this.selected == 'Ugovor'){
      this.sent=true;
      console.log(this.file);
      if(this.file){
        this.fileService.parseFile(this.file).subscribe({
          next:(data) => {
            this.contract = data;
            console.log(this.contract)
          },error:(err) => {
            console.log(err)
          }
        });
      }
    }else{
      this.indexFile();
    }

  }

  indexFile(){
    if(this.file){
      this.indexService.indexFile(this.file,this.selected.toLowerCase()).subscribe({
        next:(data) => {
          console.log(data)
          alert('ok');
        },error:(err) => {
          console.log(err)
        }
      });
    }
  }
}
