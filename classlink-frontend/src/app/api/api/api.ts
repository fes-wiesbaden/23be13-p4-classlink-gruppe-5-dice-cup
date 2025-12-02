export * from './admin-invitation-controller.service';
import {AdminInvitationControllerService} from './admin-invitation-controller.service';

export * from './auth-controller.service';
import {AuthControllerService} from './auth-controller.service';

export * from './dev-token-controller.service';
import {DevTokenControllerService} from './dev-token-controller.service';

export * from './invitation-controller.service';
import {InvitationControllerService} from './invitation-controller.service';

export * from './password-reset-controller.service';
import {PasswordResetControllerService} from './password-reset-controller.service';
export * from './user-controller.service';
import { UserControllerService } from './user-controller.service';

export const APIS = [AdminInvitationControllerService, AuthControllerService, DevTokenControllerService, InvitationControllerService, PasswordResetControllerService, UserControllerService];
