import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanificationControlComponent } from './planification-control';

describe('PlanificationControl', () => {
  let component: PlanificationControlComponent;
  let fixture: ComponentFixture<PlanificationControlComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanificationControlComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanificationControlComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
