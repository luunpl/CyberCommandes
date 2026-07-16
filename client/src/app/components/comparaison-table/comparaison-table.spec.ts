import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComparaisonTable } from './comparaison-table';

describe('ComparaisonTable', () => {
  let component: ComparaisonTable;
  let fixture: ComponentFixture<ComparaisonTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComparaisonTable]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ComparaisonTable);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
