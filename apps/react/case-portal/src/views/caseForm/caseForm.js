import QuestionCircleOutlined from '@ant-design/icons/QuestionCircleOutlined'
import { Form } from '@formio/react'
import CloseIcon from '@mui/icons-material/Close'
import MoreVertIcon from '@mui/icons-material/MoreVert'
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive'
import { Grid } from '@mui/material'
import Alert from '@mui/material/Alert'
import AppBar from '@mui/material/AppBar'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import Divider from '@mui/material/Divider'
import IconButton from '@mui/material/IconButton'
import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemText from '@mui/material/ListItemText'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'
import Slide from '@mui/material/Slide'
import Stack from '@mui/material/Stack'
import Step from '@mui/material/Step'
import StepLabel from '@mui/material/StepLabel'
import Stepper from '@mui/material/Stepper'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import TextField from '@mui/material/TextField'
import Toolbar from '@mui/material/Toolbar'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import {
  AdminState,
  AdminTransition,
  getAdminStateLabel,
  isAdminLifecycleCase,
} from 'common/adminLifecycle'
import { CaseStatus } from 'common/caseStatus'
import { StorageService } from 'plugins/storage'
import PropTypes from 'prop-types'
import React, { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ProcessDefService } from 'services/ProcessDefService'
import { Comments } from 'views/caseComment/Comments'
import { CaseEmailsList } from 'views/caseEmail/caseEmailList'
import { CaseService, FormService } from '../../services'
import { tryParseJSONObject } from '../../utils/jsonStringCheck'
import { TaskList } from '../taskList/taskList'
import Documents from './Documents'

export const CaseForm = ({ open, handleClose, aCase, keycloak }) => {
  const [caseDef, setCaseDef] = useState(null)
  const [caseData, setCaseData] = useState(null)
  const [form, setForm] = useState(null)
  const [formData, setFormData] = useState(null)
  const [comments, setComments] = useState(null)
  const [documents, setDocuments] = useState(null)
  const [mainTabIndex, setMainTabIndex] = useState(0)
  const [rightTabIndex, setRightTabIndex] = useState(0)
  const [activeStage, setActiveStage] = React.useState(0)
  const [stages, setStages] = useState([])
  const { t } = useTranslation()

  const [anchorEl, setAnchorEl] = React.useState(null)
  const isMenuOpen = Boolean(anchorEl)

  const [openProcessesDialog, setOpenProcessesDialog] = useState(false)
  const [manualInitProcessDefs, setManualInitProcessDefs] = useState([])
  const [transitionDialog, setTransitionDialog] = useState({
    open: false,
    transitionName: '',
    title: '',
    values: {
      note: '',
      adminOwnerId: '',
      adminOwnerName: '',
      responsibleLawyerId: '',
      responsibleLawyerName: '',
      nextActionOwnerType: '',
      nextActionOwnerRef: '',
      nextActionSummary: '',
      nextActionDueAt: '',
      waitingReasonCode: '',
      waitingReasonText: '',
      expectedResponseAt: '',
      externalPartyRef: '',
    },
  })
  const [transitionError, setTransitionError] = useState('')

  const [isFollowing, setIsFollowing] = useState(false)
  const handleFollowClick = () => {
    setIsFollowing(!isFollowing)
  }

  useEffect(() => {
    getCaseInfo(aCase)
  }, [open, aCase])

  useEffect(() => {
    if (activeStage && caseDef) {
      const stage = caseDef.stages.find((o) => o.name === activeStage)
      const stageProcesses = stage ? stage.processesDefinitions : []
      const autoStartProcesses = stageProcesses
        ? stageProcesses.filter((o) => o.autoStart === false)
        : undefined
      setManualInitProcessDefs(autoStartProcesses)
    }
  }, [activeStage])

  const handleMenuOpen = (event) => {
    setAnchorEl(event.currentTarget)
  }

  const handleMenuClose = () => {
    setAnchorEl(null)
  }

  const getCaseInfo = (aCase) => {
    if (!aCase) {
      return
    }

    CaseService.getCaseDefinitionsById(keycloak, aCase.caseDefinitionId)
      .then((data) => {
        setCaseDef(data)
        setStages(
          data.stages.sort((a, b) => a.index - b.index).map((o) => o.name),
        )
        return FormService.getByKey(keycloak, data.formKey)
      })
      .then((data) => {
        setForm(data)
        return CaseService.getCaseById(keycloak, aCase.businessKey)
      })
      .then((caseData) => {
        setCaseData(caseData)
        setComments(
          caseData?.comments?.sort(
            (a, b) =>
              new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
          ),
        )
        setDocuments(caseData?.documents)
        setFormData({
          data: (caseData.attributes || []).reduce(
            (obj, item) =>
              Object.assign(obj, {
                [item.name]: tryParseJSONObject(item.value)
                  ? JSON.parse(item.value)
                  : item.value,
              }),
            {},
          ),
          metadata: {},
          isValid: true,
        })
        setActiveStage(caseData.stage)
      })
      .catch((err) => {
        console.log(err.message)
      })
  }

  const handleMainTabChanged = (event, newValue) => {
    setMainTabIndex(newValue)
  }

  const handleRightTabChanged = (event, newValue) => {
    setRightTabIndex(newValue)
  }

  const handleUpdateCaseStatus = (newStatus) => {
    CaseService.patch(
      keycloak,
      caseData.businessKey,
      JSON.stringify({
        status: newStatus,
      }),
    )
      .then(() => {
        handleClose()
      })
      .catch((err) => {
        console.log(err.message)
      })
  }

  const updateActiveState = () => {
    getCaseInfo(caseData || aCase)
  }

  const handleOpenProcessesDialog = () => {
    setOpenProcessesDialog(true)
    handleMenuClose()
  }

  const handleCloseProcessesDialog = () => {
    setOpenProcessesDialog(false)
  }

  const startProcess = (key) => {
    ProcessDefService.start(keycloak, key, caseData.businessKey)

    // Close the dialog
    handleCloseProcessesDialog()
  }

  const adminLifecycleCase = isAdminLifecycleCase(caseData || aCase)
  const canAdminAct =
    keycloak.hasRealmRole?.('ops_admin') ||
    keycloak.hasRealmRole?.('ops_manager')
  const currentSubjectId =
    keycloak.idTokenParsed?.sub || keycloak.tokenParsed?.sub || ''
  const canLawyerAct =
    keycloak.hasRealmRole?.('ops_manager') ||
    (keycloak.hasRealmRole?.('lawyer_user') &&
      currentSubjectId &&
      currentSubjectId === caseData?.responsibleLawyerId)

  const openTransition = (transitionName) => {
    const defaults = {
      title: transitionName,
      values: {
        note: '',
        adminOwnerId: caseData?.adminOwnerId || '',
        adminOwnerName: caseData?.adminOwnerName || '',
        responsibleLawyerId: caseData?.responsibleLawyerId || '',
        responsibleLawyerName: caseData?.responsibleLawyerName || '',
        nextActionOwnerType: caseData?.nextActionOwnerType || '',
        nextActionOwnerRef: caseData?.nextActionOwnerRef || '',
        nextActionSummary: caseData?.nextActionSummary || '',
        nextActionDueAt: caseData?.nextActionDueAt || '',
        waitingReasonCode: '',
        waitingReasonText: '',
        expectedResponseAt: caseData?.expectedResponseAt || '',
        externalPartyRef: caseData?.externalPartyRef || '',
      },
    }

    if (transitionName === AdminTransition.MarkAwaitingEngagement) {
      defaults.title = 'Mark Awaiting Engagement'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Await engagement materials'
    }
    if (transitionName === AdminTransition.MarkReadyToOpen) {
      defaults.title = 'Mark Ready to Open'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Prepare matter for lawyer review'
    }
    if (transitionName === AdminTransition.SendToLawyerReview) {
      defaults.title = 'Send to Lawyer Review'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Lawyer review required before open'
    }
    if (transitionName === AdminTransition.LawyerApproveOpen) {
      defaults.title = 'Approve Open'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary ||
        'Matter opened and ready for maintenance activation'
    }
    if (transitionName === AdminTransition.LawyerReturnForFixes) {
      defaults.title = 'Return for Fixes'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary ||
        'Lawyer returned the matter for admin fixes'
    }
    if (transitionName === AdminTransition.StartClientWait) {
      defaults.title = 'Start Client Wait'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Await client response'
    }
    if (transitionName === AdminTransition.ResumeFromClientWait) {
      defaults.title = 'Resume from Client Wait'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary ||
        'Client responded and opening work can resume'
    }
    if (transitionName === AdminTransition.ActivateMatter) {
      defaults.title = 'Activate Matter'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Active matter control in progress'
    }
    if (transitionName === AdminTransition.UpdateMaintenanceControl) {
      defaults.title = 'Update Maintenance Control'
    }
    if (transitionName === AdminTransition.SendToMaintenanceLawyerReview) {
      defaults.title = 'Send to Lawyer Review'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary ||
        'Lawyer review required for active matter'
    }
    if (transitionName === AdminTransition.LawyerReturnToActive) {
      defaults.title = 'Return to Active'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary ||
        'Lawyer review complete and matter is active'
    }
    if (transitionName === AdminTransition.StartMaintenanceClientWait) {
      defaults.title = 'Start Client Wait'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Await client response'
    }
    if (transitionName === AdminTransition.ResumeFromMaintenanceClientWait) {
      defaults.title = 'Resume from Client Wait'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary ||
        'Follow-up received and active work can resume'
    }
    if (transitionName === AdminTransition.StartExternalWait) {
      defaults.title = 'Start External Wait'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Await external response'
    }
    if (transitionName === AdminTransition.ResumeFromExternalWait) {
      defaults.title = 'Resume from External Wait'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary ||
        'Follow-up received and active work can resume'
    }
    if (transitionName === AdminTransition.LawyerRequestClientFollowup) {
      defaults.title = 'Request Client Follow-up'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Lawyer requested client follow-up'
    }
    if (transitionName === AdminTransition.LawyerRequestExternalFollowup) {
      defaults.title = 'Request External Follow-up'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Await external response'
    }
    if (transitionName === AdminTransition.StartClosingReview) {
      defaults.title = 'Start Closing Review'
      defaults.values.nextActionSummary =
        caseData?.nextActionSummary || 'Complete final closing review'
    }
    if (transitionName === AdminTransition.CloseMatter) {
      defaults.title = 'Close Matter'
      defaults.values.nextActionSummary = ''
    }
    if (transitionName === AdminTransition.ArchiveMatter) {
      defaults.title = 'Archive Matter'
      defaults.values.nextActionSummary = ''
    }

    setTransitionError('')
    setTransitionDialog({
      open: true,
      transitionName,
      title: defaults.title,
      values: defaults.values,
    })
  }

  const handleTransitionDialogClose = () => {
    setTransitionDialog((previous) => ({ ...previous, open: false }))
    setTransitionError('')
  }

  const handleTransitionFieldChange = (field, value) => {
    setTransitionDialog((previous) => ({
      ...previous,
      values: {
        ...previous.values,
        [field]: value,
      },
    }))
  }

  const submitTransition = () => {
    const body = {
      actorName:
        keycloak.idTokenParsed?.name ||
        keycloak.tokenParsed?.given_name ||
        keycloak.tokenParsed?.preferred_username,
      note: transitionDialog.values.note,
      adminOwnerId: transitionDialog.values.adminOwnerId,
      adminOwnerName: transitionDialog.values.adminOwnerName,
      responsibleLawyerId: transitionDialog.values.responsibleLawyerId,
      responsibleLawyerName: transitionDialog.values.responsibleLawyerName,
      nextActionOwnerType: transitionDialog.values.nextActionOwnerType,
      nextActionOwnerRef: transitionDialog.values.nextActionOwnerRef,
      nextActionSummary: transitionDialog.values.nextActionSummary,
      nextActionDueAt: transitionDialog.values.nextActionDueAt,
      waitingReasonCode: transitionDialog.values.waitingReasonCode,
      waitingReasonText: transitionDialog.values.waitingReasonText,
      expectedResponseAt: transitionDialog.values.expectedResponseAt,
      externalPartyRef: transitionDialog.values.externalPartyRef,
    }

    CaseService.transition(
      keycloak,
      caseData.businessKey,
      transitionDialog.transitionName,
      body,
    )
      .then(() => {
        handleTransitionDialogClose()
        getCaseInfo(caseData)
      })
      .catch(async (err) => {
        const message =
          err?.message ||
          (err?.text && (await err.text?.())) ||
          'Transition failed'
        setTransitionError(message)
      })
  }

  const renderAdminActions = () => {
    if (!adminLifecycleCase || !caseData) {
      return null
    }

    const state = caseData.adminState
    const actions = []

    if (canAdminAct && [AdminState.IntakeReview].includes(state)) {
      actions.push(
        <Button
          key='awaiting-engagement'
          color='inherit'
          onClick={() => openTransition(AdminTransition.MarkAwaitingEngagement)}
        >
          Await Engagement
        </Button>,
      )
      actions.push(
        <Button
          key='ready-to-open'
          color='inherit'
          onClick={() => openTransition(AdminTransition.MarkReadyToOpen)}
        >
          Ready to Open
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.AwaitingEngagement) {
      actions.push(
        <Button
          key='awaiting-ready'
          color='inherit'
          onClick={() => openTransition(AdminTransition.MarkReadyToOpen)}
        >
          Ready to Open
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.ReadyToOpen) {
      actions.push(
        <Button
          key='send-lawyer'
          color='inherit'
          onClick={() => openTransition(AdminTransition.SendToLawyerReview)}
        >
          Send to Lawyer
        </Button>,
      )
      actions.push(
        <Button
          key='start-client-wait-ready'
          color='inherit'
          onClick={() => openTransition(AdminTransition.StartClientWait)}
        >
          Wait on Client
        </Button>,
      )
    }

    if (canLawyerAct && state === AdminState.ReadyForLawyer) {
      actions.push(
        <Button
          key='approve-open'
          color='inherit'
          onClick={() => openTransition(AdminTransition.LawyerApproveOpen)}
        >
          Approve Open
        </Button>,
      )
      actions.push(
        <Button
          key='return-fixes'
          color='inherit'
          onClick={() => openTransition(AdminTransition.LawyerReturnForFixes)}
        >
          Return for Fixes
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.ReadyForLawyer) {
      actions.push(
        <Button
          key='start-client-wait-lawyer'
          color='inherit'
          onClick={() => openTransition(AdminTransition.StartClientWait)}
        >
          Wait on Client
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.WaitingOnClient) {
      actions.push(
        <Button
          key='resume-client-wait'
          color='inherit'
          onClick={() => openTransition(AdminTransition.ResumeFromClientWait)}
        >
          Resume
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.Opened) {
      actions.push(
        <Button
          key='activate-matter'
          color='inherit'
          onClick={() => openTransition(AdminTransition.ActivateMatter)}
        >
          Activate Matter
        </Button>,
      )
      actions.push(
        <Button
          key='update-opened-control'
          color='inherit'
          onClick={() =>
            openTransition(AdminTransition.UpdateMaintenanceControl)
          }
        >
          Update Control
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.Active) {
      actions.push(
        <Button
          key='maintenance-lawyer-review'
          color='inherit'
          onClick={() =>
            openTransition(AdminTransition.SendToMaintenanceLawyerReview)
          }
        >
          Send to Lawyer
        </Button>,
      )
      actions.push(
        <Button
          key='maintenance-client-wait'
          color='inherit'
          onClick={() =>
            openTransition(AdminTransition.StartMaintenanceClientWait)
          }
        >
          Wait on Client
        </Button>,
      )
      actions.push(
        <Button
          key='external-wait'
          color='inherit'
          onClick={() => openTransition(AdminTransition.StartExternalWait)}
        >
          Wait on External
        </Button>,
      )
      actions.push(
        <Button
          key='update-active-control'
          color='inherit'
          onClick={() =>
            openTransition(AdminTransition.UpdateMaintenanceControl)
          }
        >
          Update Control
        </Button>,
      )
      actions.push(
        <Button
          key='start-closing-review'
          color='inherit'
          onClick={() => openTransition(AdminTransition.StartClosingReview)}
        >
          Start Closing Review
        </Button>,
      )
    }

    if (canLawyerAct && state === AdminState.MaintenanceLawyerReview) {
      actions.push(
        <Button
          key='maintenance-return-active'
          color='inherit'
          onClick={() => openTransition(AdminTransition.LawyerReturnToActive)}
        >
          Return to Active
        </Button>,
      )
      actions.push(
        <Button
          key='lawyer-client-followup'
          color='inherit'
          onClick={() =>
            openTransition(AdminTransition.LawyerRequestClientFollowup)
          }
        >
          Request Client Follow-up
        </Button>,
      )
      actions.push(
        <Button
          key='lawyer-external-followup'
          color='inherit'
          onClick={() =>
            openTransition(AdminTransition.LawyerRequestExternalFollowup)
          }
        >
          Request External Follow-up
        </Button>,
      )
    }

    if (
      canAdminAct &&
      [
        AdminState.MaintenanceLawyerReview,
        AdminState.MaintenanceClientWait,
        AdminState.WaitingOnExternal,
      ].includes(state)
    ) {
      actions.push(
        <Button
          key={`update-${state}`}
          color='inherit'
          onClick={() =>
            openTransition(AdminTransition.UpdateMaintenanceControl)
          }
        >
          Update Control
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.MaintenanceClientWait) {
      actions.push(
        <Button
          key='resume-maintenance-client-wait'
          color='inherit'
          onClick={() =>
            openTransition(AdminTransition.ResumeFromMaintenanceClientWait)
          }
        >
          Resume
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.WaitingOnExternal) {
      actions.push(
        <Button
          key='resume-external-wait'
          color='inherit'
          onClick={() => openTransition(AdminTransition.ResumeFromExternalWait)}
        >
          Resume
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.ClosingReview) {
      actions.push(
        <Button
          key='close-matter'
          color='inherit'
          onClick={() => openTransition(AdminTransition.CloseMatter)}
        >
          Close Matter
        </Button>,
      )
    }

    if (canAdminAct && state === AdminState.Closed) {
      actions.push(
        <Button
          key='archive-matter'
          color='inherit'
          onClick={() => openTransition(AdminTransition.ArchiveMatter)}
        >
          Archive Matter
        </Button>,
      )
    }

    return actions
  }

  const needsClientWaitFields = [
    AdminTransition.StartClientWait,
    AdminTransition.StartMaintenanceClientWait,
    AdminTransition.LawyerRequestClientFollowup,
  ].includes(transitionDialog.transitionName)

  const needsExternalWaitFields = [
    AdminTransition.StartExternalWait,
    AdminTransition.LawyerRequestExternalFollowup,
  ].includes(transitionDialog.transitionName)

  const needsOwnerControls = [
    AdminTransition.UpdateMaintenanceControl,
    AdminTransition.SendToLawyerReview,
    AdminTransition.SendToMaintenanceLawyerReview,
  ].includes(transitionDialog.transitionName)

  const needsNextActionDueDate = [
    AdminTransition.MarkAwaitingEngagement,
    AdminTransition.MarkReadyToOpen,
    AdminTransition.SendToLawyerReview,
    AdminTransition.LawyerReturnForFixes,
    AdminTransition.ResumeFromClientWait,
    AdminTransition.ActivateMatter,
    AdminTransition.UpdateMaintenanceControl,
    AdminTransition.SendToMaintenanceLawyerReview,
    AdminTransition.LawyerReturnToActive,
    AdminTransition.ResumeFromMaintenanceClientWait,
    AdminTransition.ResumeFromExternalWait,
    AdminTransition.StartClosingReview,
  ].includes(transitionDialog.transitionName)

  const hidesNextActionSummary = [
    AdminTransition.LawyerApproveOpen,
    AdminTransition.CloseMatter,
    AdminTransition.ArchiveMatter,
  ].includes(transitionDialog.transitionName)

  const needsNextActionRouting =
    transitionDialog.transitionName === AdminTransition.UpdateMaintenanceControl

  return (
    aCase &&
    caseDef &&
    form &&
    formData &&
    caseData && (
      <div>
        <Dialog
          fullScreen
          open={open}
          onClose={handleClose}
          TransitionComponent={Transition}
        >
          <AppBar sx={{ position: 'relative' }}>
            <Toolbar>
              <IconButton
                edge='start'
                color='inherit'
                onClick={handleClose}
                aria-label='close'
              >
                <CloseIcon />
              </IconButton>
              <Typography sx={{ ml: 2, flex: 1 }} component='div'>
                <div>
                  {caseDef.name}: {caseData?.businessKey}
                </div>
                <div style={{ fontSize: '13px' }}>
                  {caseData?.statusDescription}
                </div>
              </Typography>
              {!adminLifecycleCase &&
                caseData.status === CaseStatus.WipCaseStatus.description && (
                  <Button
                    color='inherit'
                    onClick={() =>
                      handleUpdateCaseStatus(
                        CaseStatus.ClosedCaseStatus.description,
                      )
                    }
                  >
                    {t('pages.caseform.actions.close')}
                  </Button>
                )}
              {!adminLifecycleCase &&
                caseData.status === CaseStatus.ClosedCaseStatus.description && (
                  <React.Fragment>
                    <Button
                      color='inherit'
                      onClick={() =>
                        handleUpdateCaseStatus(
                          CaseStatus.WipCaseStatus.description,
                        )
                      }
                    >
                      {t('pages.caseform.actions.reopen')}
                    </Button>

                    <Button
                      color='inherit'
                      onClick={() =>
                        handleUpdateCaseStatus(
                          CaseStatus.ArchivedCaseStatus.description,
                        )
                      }
                    >
                      {t('pages.caseform.actions.archive')}
                    </Button>
                  </React.Fragment>
                )}
              {!adminLifecycleCase &&
                caseData.status ===
                  CaseStatus.ArchivedCaseStatus.description && (
                  <React.Fragment>
                    <Button
                      color='inherit'
                      onClick={() =>
                        handleUpdateCaseStatus(
                          CaseStatus.WipCaseStatus.description,
                        )
                      }
                    >
                      {t('pages.caseform.actions.reopen')}
                    </Button>
                  </React.Fragment>
                )}
              {renderAdminActions()}
              <Button
                color='inherit'
                onClick={handleFollowClick}
                startIcon={<NotificationsActiveIcon />}
              >
                {isFollowing ? 'Unfollow' : 'Follow'}
              </Button>

              {/* Case Actions Menu */}
              <IconButton
                edge='end'
                color='inherit'
                onClick={handleMenuOpen}
                aria-label='manual-actions'
              >
                <MoreVertIcon />
              </IconButton>
              <Menu
                anchorEl={anchorEl}
                anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
                id='manual-actions-menu'
                keepMounted
                transformOrigin={{ vertical: 'top', horizontal: 'right' }}
                open={isMenuOpen}
                onClose={handleMenuClose}
              >
                {
                  <MenuItem onClick={handleOpenProcessesDialog}>
                    {t('pages.caseform.actions.startProcess')}
                  </MenuItem>
                }
              </Menu>
            </Toolbar>
          </AppBar>

          <Box
            sx={{
              pl: 10,
              pr: 10,
              pt: 2,
              pb: 2,
              borderBottom: 1,
              borderColor: 'divider',
            }}
          >
            <Stepper
              activeStep={stages.findIndex((o) => {
                return o === activeStage
              })}
            >
              {stages.map((label) => {
                const stagesProps = {}
                const labelProps = {}
                return (
                  <Step key={label} {...stagesProps}>
                    <StepLabel {...labelProps}>{label}</StepLabel>
                  </Step>
                )
              })}
            </Stepper>
          </Box>

          <Grid container spacing={2} sx={{ paddingLeft: 1, paddingRight: 1 }}>
            <Grid item xs={12} sm={8}>
              <Box>
                <Tabs value={mainTabIndex} onChange={handleMainTabChanged}>
                  <Tab
                    label={t('pages.caseform.tabs.details')}
                    {...a11yProps(0)}
                  />
                </Tabs>
              </Box>
              <Box
                sx={{ border: 1, borderColor: 'divider', borderRadius: '5px' }}
              >
                <TabPanel value={mainTabIndex} index={0}>
                  {adminLifecycleCase && (
                    <Box sx={{ mb: 3 }}>
                      <Typography variant='h5' sx={{ mb: 2 }}>
                        Admin Control
                      </Typography>
                      <Stack direction='row' spacing={1} sx={{ mb: 2 }}>
                        <Chip
                          label={`State: ${
                            caseData.adminStateDescription ||
                            getAdminStateLabel(caseData.adminState)
                          }`}
                        />
                        <Chip
                          label={`Health: ${caseData.adminHealth || '-'}`}
                        />
                        <Chip
                          label={`Next Owner: ${
                            caseData.nextActionOwnerTypeDescription ||
                            caseData.nextActionOwnerType ||
                            '-'
                          }`}
                        />
                      </Stack>
                      {caseData.healthReasonCodes?.length > 0 && (
                        <Alert severity='info' sx={{ mb: 2 }}>
                          {caseData.healthReasonCodes.join(', ')}
                        </Alert>
                      )}
                      <Stack spacing={1}>
                        <Typography variant='body2'>
                          Next action: {caseData.nextActionSummary || '-'}
                        </Typography>
                        <Typography variant='body2'>
                          Queue: {caseData.queueId || '-'}
                        </Typography>
                        <Typography variant='body2'>
                          Responsible lawyer:{' '}
                          {caseData.responsibleLawyerName ||
                            caseData.responsibleLawyerId ||
                            '-'}
                        </Typography>
                        <Typography variant='body2'>
                          Admin owner:{' '}
                          {caseData.adminOwnerName ||
                            caseData.adminOwnerId ||
                            '-'}
                        </Typography>
                        <Typography variant='body2'>
                          Waiting since: {caseData.waitingSince || '-'}
                        </Typography>
                        <Typography variant='body2'>
                          Resume to state:{' '}
                          {caseData.resumeToStateDescription ||
                            getAdminStateLabel(caseData.resumeToState)}
                        </Typography>
                        <Typography variant='body2'>
                          External party: {caseData.externalPartyRef || '-'}
                        </Typography>
                      </Stack>
                    </Box>
                  )}
                  {/* Case Details  */}
                  <Grid
                    container
                    spacing={2}
                    sx={{ display: 'flex', flexDirection: 'column' }}
                  >
                    <Box
                      sx={{
                        pb: 1,
                        display: 'flex',
                        flexDirection: 'row',
                      }}
                    >
                      <Typography
                        variant='h5'
                        color='textSecondary'
                        sx={{ pr: 0.5 }}
                      >
                        {form.title}
                      </Typography>
                      <Tooltip title={form.toolTip}>
                        <QuestionCircleOutlined />
                      </Tooltip>
                    </Box>
                    <Form
                      form={form.structure}
                      submission={formData}
                      options={{
                        readOnly: true,
                        fileService: new StorageService(),
                      }}
                    />
                  </Grid>
                </TabPanel>
              </Box>
            </Grid>

            <Grid item xs={12} sm={4}>
              <Box>
                <Tabs value={rightTabIndex} onChange={handleRightTabChanged}>
                  <Tab
                    label={t('pages.caseform.tabs.tasks')}
                    {...a11yProps(0)}
                  />
                  <Tab
                    label={t('pages.caseform.tabs.emails')}
                    {...a11yProps(1)}
                  />
                  <Tab
                    label={t('pages.caseform.tabs.attachments')}
                    {...a11yProps(2)}
                  />
                  <Tab
                    label={t('pages.caseform.tabs.comments')}
                    {...a11yProps(3)}
                  />
                  <Tab label='Audit' {...a11yProps(4)} />
                </Tabs>
              </Box>
              <Box
                sx={{ border: 1, borderColor: 'divider', borderRadius: '5px' }}
              >
                <TabPanel value={rightTabIndex} index={0}>
                  <TaskList
                    businessKey={caseData.businessKey}
                    callback={updateActiveState}
                  />
                </TabPanel>

                <TabPanel value={rightTabIndex} index={1}>
                  <CaseEmailsList
                    caseInstanceBusinessKey={caseData.businessKey}
                  />
                </TabPanel>

                <TabPanel value={rightTabIndex} index={2}>
                  <Documents aCase={caseData} initialValue={documents || []} />
                </TabPanel>

                <TabPanel value={rightTabIndex} index={3}>
                  <Grid
                    container
                    spacing={2}
                    sx={{ display: 'flex', flexDirection: 'column' }}
                  >
                    <Grid item xs={12}>
                      <Comments
                        aCase={caseData}
                        getCaseInfo={getCaseInfo}
                        comments={comments ? comments : []}
                      />
                    </Grid>
                  </Grid>
                </TabPanel>
                <TabPanel value={rightTabIndex} index={4}>
                  <List>
                    {(caseData.adminEvents || []).map((event, index) => (
                      <React.Fragment key={`${event.eventType}-${index}`}>
                        <ListItem>
                          <ListItemText
                            primary={`${event.eventType} ${
                              event.toState
                                ? `to ${getAdminStateLabel(event.toState)}`
                                : ''
                            }`}
                            secondary={`${event.occurredAt || ''} ${
                              event.note ? `- ${event.note}` : ''
                            }`}
                          />
                        </ListItem>
                        {index !== (caseData.adminEvents || []).length - 1 && (
                          <Divider />
                        )}
                      </React.Fragment>
                    ))}
                  </List>
                </TabPanel>
              </Box>
            </Grid>
          </Grid>
        </Dialog>

        {manualInitProcessDefs && (
          <Dialog
            onClose={handleCloseProcessesDialog}
            open={openProcessesDialog}
          >
            <DialogTitle sx={{ paddingBottom: 2 }}>
              {t('pages.caseform.manualProcesses.title')}
            </DialogTitle>
            <List>
              {manualInitProcessDefs.map((process, index) => (
                <React.Fragment key={process.definitionKey}>
                  <ListItem
                    button
                    onClick={() => startProcess(process.definitionKey)}
                    sx={{
                      '&:hover': {
                        backgroundColor: 'action.hover',
                      },
                    }}
                  >
                    <ListItemText
                      primary={process.name || process.definitionKey}
                    />
                  </ListItem>
                  {index !== manualInitProcessDefs.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Dialog>
        )}

        <Dialog
          open={transitionDialog.open}
          onClose={handleTransitionDialogClose}
          fullWidth
          maxWidth='sm'
        >
          <DialogTitle>{transitionDialog.title}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              {transitionError && (
                <Alert severity='error'>{transitionError}</Alert>
              )}
              <TextField
                label='Note'
                multiline
                minRows={3}
                value={transitionDialog.values.note}
                onChange={(event) =>
                  handleTransitionFieldChange('note', event.target.value)
                }
              />
              {!hidesNextActionSummary && (
                <TextField
                  label='Next Action Summary'
                  value={transitionDialog.values.nextActionSummary}
                  onChange={(event) =>
                    handleTransitionFieldChange(
                      'nextActionSummary',
                      event.target.value,
                    )
                  }
                />
              )}
              {needsOwnerControls && (
                <React.Fragment>
                  <TextField
                    label='Admin Owner Id'
                    value={transitionDialog.values.adminOwnerId}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'adminOwnerId',
                        event.target.value,
                      )
                    }
                  />
                  <TextField
                    label='Admin Owner Name'
                    value={transitionDialog.values.adminOwnerName}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'adminOwnerName',
                        event.target.value,
                      )
                    }
                  />
                  <TextField
                    label='Responsible Lawyer Id'
                    value={transitionDialog.values.responsibleLawyerId}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'responsibleLawyerId',
                        event.target.value,
                      )
                    }
                  />
                  <TextField
                    label='Responsible Lawyer Name'
                    value={transitionDialog.values.responsibleLawyerName}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'responsibleLawyerName',
                        event.target.value,
                      )
                    }
                  />
                </React.Fragment>
              )}
              {needsNextActionRouting && (
                <React.Fragment>
                  <TextField
                    label='Next Action Owner Type'
                    value={transitionDialog.values.nextActionOwnerType}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'nextActionOwnerType',
                        event.target.value,
                      )
                    }
                    helperText='Admin, Lawyer, Client, External, or System'
                  />
                  <TextField
                    label='Next Action Owner Ref'
                    value={transitionDialog.values.nextActionOwnerRef}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'nextActionOwnerRef',
                        event.target.value,
                      )
                    }
                  />
                </React.Fragment>
              )}
              {needsNextActionDueDate && (
                <TextField
                  label='Next Action Due Date'
                  type='date'
                  InputLabelProps={{ shrink: true }}
                  value={transitionDialog.values.nextActionDueAt}
                  onChange={(event) =>
                    handleTransitionFieldChange(
                      'nextActionDueAt',
                      event.target.value,
                    )
                  }
                />
              )}
              {[
                AdminTransition.UpdateMaintenanceControl,
                AdminTransition.StartExternalWait,
                AdminTransition.LawyerRequestExternalFollowup,
              ].includes(transitionDialog.transitionName) && (
                <TextField
                  label='External Party Ref'
                  value={transitionDialog.values.externalPartyRef}
                  onChange={(event) =>
                    handleTransitionFieldChange(
                      'externalPartyRef',
                      event.target.value,
                    )
                  }
                />
              )}
              {needsClientWaitFields && (
                <React.Fragment>
                  <TextField
                    label='Waiting Reason Code'
                    value={transitionDialog.values.waitingReasonCode}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'waitingReasonCode',
                        event.target.value,
                      )
                    }
                  />
                  <TextField
                    label='Waiting Reason Detail'
                    value={transitionDialog.values.waitingReasonText}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'waitingReasonText',
                        event.target.value,
                      )
                    }
                  />
                  <TextField
                    label='Expected Response Date'
                    type='date'
                    InputLabelProps={{ shrink: true }}
                    value={transitionDialog.values.expectedResponseAt}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'expectedResponseAt',
                        event.target.value,
                      )
                    }
                  />
                </React.Fragment>
              )}
              {needsExternalWaitFields && (
                <React.Fragment>
                  <TextField
                    label='Waiting Reason Code'
                    value={transitionDialog.values.waitingReasonCode}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'waitingReasonCode',
                        event.target.value,
                      )
                    }
                  />
                  <TextField
                    label='Waiting Reason Detail'
                    value={transitionDialog.values.waitingReasonText}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'waitingReasonText',
                        event.target.value,
                      )
                    }
                  />
                  <TextField
                    label='Expected Response Date'
                    type='date'
                    InputLabelProps={{ shrink: true }}
                    value={transitionDialog.values.expectedResponseAt}
                    onChange={(event) =>
                      handleTransitionFieldChange(
                        'expectedResponseAt',
                        event.target.value,
                      )
                    }
                  />
                </React.Fragment>
              )}
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleTransitionDialogClose}>Cancel</Button>
            <Button onClick={submitTransition} variant='contained'>
              Apply
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    )
  )
}

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide direction='up' ref={ref} {...props} />
})

function a11yProps(index) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  }
}

function TabPanel(props) {
  const { children, value, index, ...other } = props

  return (
    <div
      role='tabpanel'
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          <Typography component={'span'}>{children}</Typography>
        </Box>
      )}
    </div>
  )
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.number.isRequired,
  value: PropTypes.number.isRequired,
}
