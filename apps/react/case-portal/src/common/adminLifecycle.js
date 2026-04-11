export const ADMIN_OPENING_CONTROL_CASE_DEF_ID = 'matter-admin-opening-control'

export const AdminState = {
  IntakeReview: 'Intake Review',
  AwaitingEngagement: 'Awaiting Engagement',
  ReadyToOpen: 'Ready to Open',
  ReadyForLawyer: 'Ready for Lawyer',
  WaitingOnClient: 'Waiting on Client',
  Opened: 'Opened',
  Active: 'Active',
  MaintenanceLawyerReview: 'Maintenance Lawyer Review',
  MaintenanceClientWait: 'Maintenance Client Wait',
  WaitingOnExternal: 'Waiting on External',
  Open: 'Opened',
}

export const AdminTransition = {
  SubmitIntakeReview: 'submitIntakeReview',
  MarkAwaitingEngagement: 'markAwaitingEngagement',
  MarkReadyToOpen: 'markReadyToOpen',
  SendToLawyerReview: 'sendToLawyerReview',
  LawyerApproveOpen: 'lawyerApproveOpen',
  LawyerReturnForFixes: 'lawyerReturnForFixes',
  StartClientWait: 'startClientWait',
  ResumeFromClientWait: 'resumeFromClientWait',
  ActivateMatter: 'activateMatter',
  UpdateMaintenanceControl: 'updateMaintenanceControl',
  SendToMaintenanceLawyerReview: 'sendToMaintenanceLawyerReview',
  LawyerReturnToActive: 'lawyerReturnToActive',
  StartMaintenanceClientWait: 'startMaintenanceClientWait',
  ResumeFromMaintenanceClientWait: 'resumeFromMaintenanceClientWait',
  StartExternalWait: 'startExternalWait',
  ResumeFromExternalWait: 'resumeFromExternalWait',
  LawyerRequestClientFollowup: 'lawyerRequestClientFollowup',
  LawyerRequestExternalFollowup: 'lawyerRequestExternalFollowup',
}

export function isAdminLifecycleCase(aCase) {
  return aCase?.caseDefinitionId === ADMIN_OPENING_CONTROL_CASE_DEF_ID
}

export function getAdminStateLabel(adminState) {
  switch (adminState) {
    case 'Open':
      return AdminState.Opened
    case AdminState.MaintenanceLawyerReview:
      return AdminState.ReadyForLawyer
    case AdminState.MaintenanceClientWait:
      return AdminState.WaitingOnClient
    default:
      return adminState || '-'
  }
}
