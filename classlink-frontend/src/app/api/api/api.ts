export * from './registration-controller.service';
import { RegistrationControllerService } from './registration-controller.service';
export * from './scalar-controller.service';
import { ScalarControllerService } from './scalar-controller.service';
export * from './user-controller.service';
import { UserControllerService } from './user-controller.service';
export const APIS = [RegistrationControllerService, ScalarControllerService, UserControllerService];
