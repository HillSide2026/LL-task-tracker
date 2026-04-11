/* eslint-disable no-undef */
import { AdminState, getAdminStateLabel } from './adminLifecycle'

test('maps legacy and maintenance admin states to display labels', () => {
  expect(getAdminStateLabel('Open')).toEqual(AdminState.Opened)
  expect(getAdminStateLabel(AdminState.MaintenanceLawyerReview)).toEqual(
    AdminState.ReadyForLawyer,
  )
  expect(getAdminStateLabel(AdminState.MaintenanceClientWait)).toEqual(
    AdminState.WaitingOnClient,
  )
  expect(getAdminStateLabel(AdminState.Active)).toEqual(AdminState.Active)
})
